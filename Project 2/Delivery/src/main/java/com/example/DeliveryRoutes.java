package com.example;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.example.dto.AgentSignInOut;
import com.example.dto.OrderDelivered;
import com.example.dto.PlaceOrder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class DeliveryRoutes {

	// private final static Logger log = LoggerFactory.getLogger(DeliveryRoutes.class);
	private final ActorRef<Delivery.Command> deliveryActor;
	private final Duration askTimeout;
	private final Scheduler scheduler;

	public DeliveryRoutes(ActorSystem<?> system, ActorRef<Delivery.Command> deliveryActor) {
		this.deliveryActor = deliveryActor;
		scheduler = system.scheduler();
		askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
	}

	private CompletionStage<Delivery.ReInitializeResponse> reInitialize() {
		// log.info("serving reInitialize request");
		return AskPattern.ask(deliveryActor, Delivery.ReInitialize::new, askTimeout, scheduler);
	}

	private CompletionStage<Delivery.AgentSignInOutResponse> agentSignIn(AgentSignInOut agentSignInOut) {
		return AskPattern.ask(deliveryActor,
				ref -> new Delivery.AgentSignInOutCommand(ref, agentSignInOut.getAgentId(), true), askTimeout,
				scheduler);
	}

	private CompletionStage<Delivery.RequestOrderResponse> requestOrder(PlaceOrder placeOrder) {
		// log.info("serving requestOrder request");
		// log.info("placeOrder: " + placeOrder);
		return AskPattern.ask(deliveryActor, ref -> new Delivery.RequestOrder(ref, placeOrder), askTimeout, scheduler);
	}

	private CompletionStage<Delivery.AgentSignInOutResponse> agentSignOut(AgentSignInOut agentSignInOut) {
		return AskPattern.ask(deliveryActor,
				ref -> new Delivery.AgentSignInOutCommand(ref, agentSignInOut.getAgentId(), false), askTimeout,
				scheduler);
	}

	private CompletionStage<Delivery.GetOrderDeliveredResp> orderDelivered(OrderDelivered orderDelivered) {
		// log.info("serving orderDelivered request");
		// log.info("orderDelivered: " + orderDelivered);
		return AskPattern.ask(deliveryActor, ref -> new Delivery.OrderDeliveredCmd(ref,orderDelivered), askTimeout, scheduler);
	}

	private CompletionStage<FulFillOrder.GetOrderResponse> getOrder(String num) {
		return AskPattern.ask(deliveryActor, ref -> new Delivery.GetOrderCmd(ref, num), askTimeout, scheduler);
	}

	private CompletionStage<Agent.GetAgentResponse> getAgent(String num) {
		return AskPattern.ask(deliveryActor, ref -> new Delivery.GetAgentCmd(ref, num), askTimeout, scheduler);
	}

	/**
	 * request - post /reInitialize
	 * response - http code 201
	 */
	Route reInitializeRoute = concat(
			pathEnd(() -> concat(
					post(() -> onSuccess(reInitialize(), (t) -> complete(StatusCodes.CREATED))))));

	/**
	 * request - post /agentSignIn
	 * requestbody - {"agentId": num}
	 * response - http code 201
	 */
	Route agentSignInRoute = concat(
			pathEnd(() -> concat(
					post(() -> entity(
							Jackson.unmarshaller(AgentSignInOut.class),
							agent -> onSuccess(agentSignIn(agent), (t) -> complete(StatusCodes.CREATED)))))));

	/**
	 * request - get /agent/num
	 * response - http code 200
	 * - {"agentId": num, "status": y}
	 */
	Route agentRoute = concat(
			path(PathMatchers.segment(), (String num) -> concat(
					get(() -> onSuccess(getAgent(num),
							performed -> complete(StatusCodes.OK, performed.agent, Jackson.marshaller()))))));

	/**
	 * request - get /order/num
	 * response - http code 404 if no order
	 * - http code 200 if has order
	 * - {"orderId":num, "status": x, "agentId": y}
	 */
	Route orderRoute = concat(
			path(PathMatchers.segment(), (String num) -> concat(
					get(() -> onSuccess(getOrder(num),
							performed -> performed.order == null
									? complete(StatusCodes.NOT_FOUND)
									: complete(StatusCodes.OK, performed.order, Jackson.marshaller()))))));

	/**
	 * request - post /orderDelivered
	 * requestbody - {"orderId": num}
	 * response - http code 201
	 */
	Route orderDeliveredRoute = concat(
			pathEnd(() -> concat(
					post(() -> entity(
							Jackson.unmarshaller(OrderDelivered.class),
							order -> onSuccess(orderDelivered(order), (t) -> complete(StatusCodes.CREATED)))))));

	/**
	 * request - post /agentSignOut
	 * requestbody - {"agentId": num}
	 * response - http code 201
	 */
	Route agentSignOutRoute = concat(
			pathEnd(() -> concat(
					post(() -> entity(
							Jackson.unmarshaller(AgentSignInOut.class),
							agent -> onSuccess(agentSignOut(agent), (t) -> complete(StatusCodes.CREATED)))))));

	/**
	 * request - post /requestOrder
	 * requestbody - {"custId": num, "restId": x, "itemId": y, "qty": z}
	 * response - http code 201 if success { "orderId": num }
	 * - http code 410 if fail
	 */
	Route requestOrderRoute = concat(
			pathEnd(() -> concat(
					post(() -> entity(
							Jackson.unmarshaller(PlaceOrder.class),
							requestOrder -> onSuccess(requestOrder(requestOrder),
									(placeOrder) -> placeOrder ==null? complete(StatusCodes.GONE)
									: complete(StatusCodes.CREATED,placeOrder,Jackson.marshaller())))))));

	/**
	 * Route : /reInitialize post
	 */
	public Route deliveryRoutes() {
		return concat(
				pathPrefix("reInitialize", () -> reInitializeRoute),
				pathPrefix("agentSignIn", () -> agentSignInRoute),
				pathPrefix("requestOrder", () -> requestOrderRoute),
				pathPrefix("agentSignOut", () -> agentSignOutRoute),
				pathPrefix("orderDelivered", () -> orderDeliveredRoute),
				pathPrefix("order", () -> orderRoute),
				pathPrefix("agent", () -> agentRoute));
	}
}