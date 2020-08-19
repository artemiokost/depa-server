package io.depa.common.event;

import io.depa.common.event.action.EventAction;
import io.depa.common.event.type.EventType;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class Event {

    private String action;
    private String type;
    private JsonObject payload;

    public Event() {
    }

    public Event(JsonObject jsonObject) {
        EventConverter.fromJson(jsonObject, this);
    }

    public static class EventBuilder {

        Event event;

        EventBuilder() {
            this.event = new Event();
        }

        public EventBuilder action(EventAction eventAction) {
            event.setAction(eventAction.name());
            return this;
        }

        public EventBuilder type(EventType type) {
            event.setType(type.name());
            return this;
        }

        public EventBuilder payload(JsonObject payload) {
            event.setPayload(payload);
            return this;
        }

        public Event build() {
            return event;
        }
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static Event createInstance(String action, EventType type, JsonObject payload) {
        Event event = new Event();
        event.setAction(action);
        event.setType(type.name());
        event.setPayload(payload);
        return event;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        EventConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    public String getAction() {
        return action;
    }
    public String getType() {
        return type;
    }
    public JsonObject getPayload() {
        return payload;
    }

    public void setAction(String action) {
        this.action = action;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }
}
