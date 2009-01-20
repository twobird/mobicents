package org.mobicents.slee.runtime.eventrouter.routingtask;

import java.util.Set;

import javax.transaction.SystemException;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.management.ServiceManagement.RuntimeService;
import org.mobicents.slee.runtime.activity.ActivityContext;
import org.mobicents.slee.runtime.eventrouter.ActivityEndEventImpl;
import org.mobicents.slee.runtime.eventrouter.DeferredEvent;
import org.mobicents.slee.runtime.eventrouter.PendingAttachementsMonitor;
import org.mobicents.slee.runtime.eventrouter.SbbInvocationState;
import org.mobicents.slee.runtime.facilities.TimerEventImpl;
import org.mobicents.slee.runtime.sbb.SbbObject;
import org.mobicents.slee.runtime.sbbentity.SbbEntity;
import org.mobicents.slee.runtime.sbbentity.SbbEntityFactory;
import org.mobicents.slee.runtime.transaction.SleeTransactionManager;

public class EventRoutingTask implements Runnable {

	private final static Logger logger = Logger.getLogger(EventRoutingTask.class);
		
	private final SleeContainer container;
	private final DeferredEvent de;
	private PendingAttachementsMonitor pendingAttachementsMonitor;
	
	private static final ActivityEndEventPostProcessor activityEndEventPostProcessor = new ActivityEndEventPostProcessor();
	private static final HandleRollback handleRollback = new HandleRollback();
	private static final HandleSbbRollback handleSbbRollback = new HandleSbbRollback();
	private static final InitialEventProcessor initialEventProcessor = new InitialEventProcessor();
	private static final NextSbbEntityFinder nextSbbEntityFinder = new NextSbbEntityFinder();
	private static final TimerEventPostProcessor timerEventPostProcessor = new TimerEventPostProcessor();
	
	public EventRoutingTask(SleeContainer container, DeferredEvent de, PendingAttachementsMonitor pendingAttachementsMonitor) {
		super(); 
		this.de = de;
		this.container = container;
		this.pendingAttachementsMonitor = pendingAttachementsMonitor;
	}

	public void run() {
		if (pendingAttachementsMonitor != null) {
			pendingAttachementsMonitor.waitTillNoTxModifyingAttachs();
		}
		if (routeQueuedEvent()) {
			container.getEventRouter().processSucessfulEventRouting(de);
		}
	}

	/**
	 * Delivers to SBBs an event off the top of the queue for an activity
	 * context
	 * 
	 * @param de
	 */
	private boolean routeQueuedEvent() {
		
		if (logger.isDebugEnabled())
			logger.debug("\n\n\nrouteTheEvent : [[[ eventId "
					+ de.getEventTypeId() + " on ac "+de.getActivityContextHandle());

		final SleeTransactionManager txMgr = this.container.getTransactionManager();
				
		boolean rollbackTx = true;
		
		try {

			// INITIAL EVENT PROCESSING
			final Set<RuntimeService> activeServices = container.getServiceManagement().getActiveServices(de.getEventTypeId());
			if (logger.isDebugEnabled()) {
				logger.debug("Active services for event "+de.getEventTypeId()+": "+activeServices);
			}
			for (RuntimeService runtimeService : activeServices) {
				initialEventProcessor.processInitialEvents(runtimeService
						.getServiceComponent(), runtimeService
						.getRootSbbDescriptor(), de, txMgr, this.container.getActivityContextFactory());
			}

			
			
			/*
			 * TODO one tx per service which declares the event type as initial
			 * and already attached services[] begin() find
			 * highestPrioritySbbEntity find highestPriorityService if
			 * (highestPriorityService.getPriority() >
			 * highestPrioritySbbEntity.getPriority()) { sbbEntity =
			 * processInitialEvent(highestPriorityService);
			 * services.remove(highestPriorityService); if (sbbEntity == null) {
			 * sbbEntity = highestPrioritySbbEntity; } }
			 * addToDeliveredSet(sbbEntity) invokeEventHandler(sbbEntity)
			 */

			// For each SBB that is attached to this activity context.
			boolean gotSbb = false;
			do {
				
				String rootSbbEntityId = null;
				ClassLoader invokerClassLoader = null;
				SbbEntity sbbEntity = null;
				SbbObject sbbObject = null;

				try {

					/*
					 * Start of SLEE Originated Invocation Sequence
					 * ============================================== This
					 * sequence consists of either: 1) One "Op Only" SLEE
					 * Originated Invocation - in the case that it's a
					 * straightforward event routing for the sbb entity. 2) One
					 * "Op and Remove" SLEE Originated Invocation - in the case
					 * it's an event routing to a root sbb entity that ends up
					 * in a remove to the same entity since the attachment count
					 * goes to zero after the event invocation 3) One "Op Only"
					 * followed by one "Remove Only" SLEE Originated Invocation -
					 * in the case it's an event routing to a non-root sbb
					 * entity that ends up in a remove to the corresponding root
					 * entity since the root attachment count goes to zero after
					 * the event invocation Each Invocation Sequence is handled
					 * in it's own transaction. All exception handling for each
					 * invocation sequence is handled here. Any exceptions that
					 * propagate up aren't necessary to be caught. -Tim
					 */

					// If this fails then we propagate up since there's nothing to roll-back anyway
					
					txMgr.begin();
					
					if (logger.isDebugEnabled()) {
						logger.debug("Delivering event "+de+" to sbb entities attached to AC "+de.getActivityContextHandle());
					}

					ActivityContext ac = null;
					Exception caught = null;
					SbbEntity highestPrioritySbbEntity = null;
					ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
					
					try {
					
						ac = container.getActivityContextFactory().getActivityContext(de.getActivityContextHandle(),true);

						try {
							highestPrioritySbbEntity = nextSbbEntityFinder.next(ac, de.getEventTypeId());
						} catch (Exception e) {
							logger.warn("Failed to find next sbb entity to deliver the event "+de+" in "+ac.getActivityContextHandle(), e);
							highestPrioritySbbEntity = null;
						}

						if (highestPrioritySbbEntity == null) {
							if (logger.isDebugEnabled()) {
								logger.debug("No more sbbs to deliver the event");
							}
							gotSbb = false;
							ac.clearDeliveredSet();						
						} else {
							gotSbb = true;
							ac.addToDeliveredSet(highestPrioritySbbEntity.getSbbEntityId());
						}

						if (gotSbb) {

							if (logger.isDebugEnabled()) {
								logger
										.debug("Highest priority SBB entity to deliver the event: "
												+ highestPrioritySbbEntity);
							}

							// CHANGE CLASS LOADER
							invokerClassLoader = highestPrioritySbbEntity.getSbbDescriptor().getClassLoader();
							Thread.currentThread().setContextClassLoader(invokerClassLoader);

							sbbEntity = highestPrioritySbbEntity;
							rootSbbEntityId = sbbEntity.getRootSbbId();

							sbbEntity.setCurrentEvent(de);

							// Assign an sbb from the pool if was not assigned
							// in initial event processing
							if (sbbEntity.getSbbObject() == null) {
								sbbEntity.assignAndActivateSbbObject();
							}

							sbbObject = sbbEntity.getSbbObject();
							sbbObject.sbbLoad();

							// GET AND CHECK EVENT MASK FOR THIS SBB ENTITY
							Set eventMask = sbbEntity.getMaskedEventTypes(de.getActivityContextHandle());
							if (!eventMask.contains(de.getEventTypeId())) {

								// TIME TO INVOKE THE EVENT HANDLER METHOD
								sbbObject.setSbbInvocationState(SbbInvocationState.INVOKING_EVENT_HANDLER);
								sbbEntity.invokeEventHandler(de,ac);

								// check to see if the transaction is marked for
								// rollback if it is then we need to get out of
								// here soon as we can.
								
								if (txMgr.getRollbackOnly()) {
									throw new Exception("The transaction is marked for rollback");
								}

								sbbObject.setSbbInvocationState(SbbInvocationState.NOT_INVOKING);

							} else {
								if (logger.isDebugEnabled()) {
									logger
											.debug("Not invoking event handler since event is masked");
								}
							}

							// IF IT'S AN ACTIVITY END EVENT DETACH SBB ENTITY HERE
							if (de.getEventTypeId().equals(ActivityEndEventImpl.getEventTypeID())) {
								highestPrioritySbbEntity.afterACDetach(de.getActivityContextHandle());
							}

							// CHECK IF WE CAN CLAIM THE ROOT SBB ENTITY
							if (rootSbbEntityId != null) {
								if (SbbEntityFactory.getSbbEntity(rootSbbEntityId).getAttachmentCount() != 0) {
									// the root sbb entity is not be claimed
									rootSbbEntityId = null;
								}
							} else {
								// it's a root sbb
								if (!sbbEntity.isRemoved()	&& sbbEntity.getAttachmentCount() == 0) {
									if (logger.isDebugEnabled()) {
										logger.debug("Attachment count for sbb entity "	+ sbbEntity.getSbbEntityId() + " is 0, removing it...");
									}
									// If it's the same entity then this is an
									// "Op and
									// Remove Invocation Sequence"
									// so we do the remove in the same
									// invocation
									// sequence as the Op
									SbbEntityFactory.removeSbbEntityWithCurrentClassLoader(sbbEntity,true);
								}
							}
						}
					} catch (Exception e) {
						logger.error("Failure while routing event; second phase. DeferredEvent ["+ de.getEventTypeId() + "]", e);
						if (highestPrioritySbbEntity != null) {
							sbbObject = highestPrioritySbbEntity.getSbbObject();
						}
						caught = e;
					} 

					// TODO emmartins review
					
					// do a final check to see if there is another SBB to
					// deliver.
					// We don't want to waste another loop. Note that
					// rollback
					// will not has any impact on this because the
					// ac.DeliveredSet
					// is not in the cache.
					if (gotSbb) {
						boolean skipAnotherLoop = false;
						try {
							if (nextSbbEntityFinder.next(ac, de.getEventTypeId()) == null) {
								skipAnotherLoop = true;
							}
						} catch (Exception e) {
							skipAnotherLoop = true;
						} finally {
							if (skipAnotherLoop) {
								gotSbb = false;
								ac.clearDeliveredSet();
							}
						}
					}

					Thread.currentThread().setContextClassLoader(
							oldClassLoader);

					boolean invokeSbbRolledBack = handleRollback.handleRollback(sbbObject,de.getEvent(),de.getLoadedAci(), caught, invokerClassLoader,txMgr);

					boolean invokeSbbRolledBackRemove = false;
					ClassLoader rootInvokerClassLoader = null;
					SbbEntity rootSbbEntity = null;

					if (!invokeSbbRolledBack && rootSbbEntityId != null) {
						/*
						 * If we get here this means that we need to do a
						 * cascading remove of the root sbb entity - since the
						 * original invocation was done on a non-root sbb entity
						 * then this is done in a different SLEE originated
						 * invocation, but inside the same SLEE originated
						 * invocation sequence. Confused yet? This is case 3) in
						 * my previous comment - the SLEE originated invocation
						 * sequence contains two SLEE originated invocations:
						 * One "Op Only" and One "Remove Only" This is the
						 * "Remove Only" part. We don't bother doing this if we
						 * already need to rollback
						 */

						caught = null;

						oldClassLoader = Thread.currentThread().getContextClassLoader();

						try {
							rootSbbEntity = SbbEntityFactory.getSbbEntity(rootSbbEntityId);

							rootInvokerClassLoader = rootSbbEntity.getSbbDescriptor().getClassLoader();
							Thread.currentThread().setContextClassLoader(rootInvokerClassLoader);

							SbbEntityFactory.removeSbbEntity(rootSbbEntity,true);

						} catch (Exception e) {
							logger.error("Failure while routing event; third phase. Event Posting ["+ de + "]", e);
							caught = e;
						} finally {

							Thread.currentThread().setContextClassLoader(oldClassLoader);
						}

						// We have no target sbb object in a Remove Only SLEE
						// originated invocation
						// FIXME emmartins review
						invokeSbbRolledBackRemove = handleRollback.handleRollback(null,de.getEvent(),de.getLoadedAci(), caught, rootInvokerClassLoader,txMgr);
					}

					/*
					 * We are now coming to the end of the SLEE originated
					 * invocation sequence We may need to run sbbRolledBack This
					 * is done in the same tx if there is no target sbb entity
					 * in any of the SLEE originated invocations making up this
					 * SLEE originated invocation sequence Otherwise we do it in
					 * a separate tx for each SLEE originated invocation that
					 * has a target sbb entity. In other words we might have a
					 * maximum of 2 sbbrolledback callbacks invoked in separate
					 * tx in the case this SLEE Originated Invocation Sequence
					 * contained an Op Only and a Remove Only (since these have
					 * different target sbb entities) Pretty obvious really! ;)
					 */
					if (invokeSbbRolledBack && sbbEntity == null) {
						// We do it in this tx
						handleSbbRollback.handleSbbRolledBack(null, sbbObject, null, null, invokerClassLoader, false, txMgr);
					} else if (sbbEntity != null && !txMgr.getRollbackOnly()
							&& sbbEntity.getSbbObject() != null) {

						sbbObject.sbbStore();
						sbbEntity.passivateAndReleaseSbbObject();

					}

					if (txMgr.getRollbackOnly()) {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Rolling back SLEE Originated Invocation Sequence");
						}
						txMgr.rollback();
					} else {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Committing SLEE Originated Invocation Sequence");
						}
						txMgr.commit();
					}

					/*
					 * Now we invoke sbbRolledBack for each SLEE originated
					 * invocation that had a target sbb entity in a new tx - the
					 * new tx creating is handled inside the handleSbbRolledBack
					 * method
					 */
					if (invokeSbbRolledBack && sbbEntity != null) {
						// Firstly for the "Op only" or "Op and Remove" part
						sbbEntity.getSbbEntityId();
						if (logger.isDebugEnabled()) {
							logger
									.debug("Invoking sbbRolledBack for Op Only or Op and Remove");

						}
						handleSbbRollback.handleSbbRolledBack(sbbEntity, null, de.getEvent(), de.getLoadedAci(), invokerClassLoader, false, txMgr);
					}
					if (invokeSbbRolledBackRemove) {
						// Now for the "Remove Only" if appropriate
						handleSbbRollback.handleSbbRolledBack(rootSbbEntity, null, null, null, rootInvokerClassLoader, true, txMgr);						
					}

					/*
					 * A note on exception handling here- Any exceptions thrown
					 * further further up that need to be caught in order to
					 * handle rollback or otherwise maintain consistency of the
					 * SLEE state are all handled further up I.e. We *do not*
					 * need to call rollback here. So any exceptions that get
					 * here do not result in the SLEE being in an inconsistent
					 * state, therefore we just log them and carry on.
					 */

					rollbackTx = false;
				} catch (RuntimeException e) {
					logger.error(
							"Unhandled RuntimeException in event router: ", e);
				} catch (Exception e) {
					logger.error("Unhandled Exception in event router: ", e);
				} catch (Error e) {
					logger.error("Unhandled Error in event router: ", e);
					throw e; // Always rethrow errors
				} catch (Throwable t) {
					logger.error("Unhandled Throwable in event router: ", t);
				} finally {
					try {
						if (txMgr.isInTx()) {
							if (rollbackTx) {
								logger
										.error("Rolling back tx in routeTheEvent.");
								txMgr.rollback();
							} else {
								logger
										.error("Transaction left open in routeTheEvent! It has to be pinned down and fixed! Debug information follows.");
								logger.error(txMgr
										.displayOngoingSleeTransactions());
								txMgr.commit();
							}
						}
					} catch (SystemException se) {

						logger.error("Failure in TX operation", se);
					}
					if (sbbEntity != null) {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Finished delivering the event "
											+ de.getEventTypeId()
											+ " to the sbbEntity = "
											+ sbbEntity.getSbbEntityId()
											+ "]]] \n\n\n");
						}
					}
				}
				// need to ensure gotSbb = false if and only if iter.hasNext()
				// == false
			} while (gotSbb);

			/*
			 * End of SLEE Originated Invocation Sequence
			 * ==========================================
			 * 
			 */

			if (de.getEventTypeId().equals(ActivityEndEventImpl.getEventTypeID())) {
				activityEndEventPostProcessor.process(de.getActivityContextHandle(), txMgr, this.container.getActivityContextFactory());
			} else if (de.getEventTypeId().equals(TimerEventImpl.getEventTypeID())) {
				timerEventPostProcessor.process(de,this.container.getTimerFacility());
			}

		} catch (Exception e) {
			logger.error("Unhandled Exception in event router top try", e);
		}

		return true;
	}

}