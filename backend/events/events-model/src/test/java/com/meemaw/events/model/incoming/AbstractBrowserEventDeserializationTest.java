package com.meemaw.events.model.incoming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.events.model.shared.BrowserEventType;
import com.meemaw.events.model.shared.LogLevel;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventDeserializationTest {

  @Test
  public void loadBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 8, \"a\": [\"http://localhost:8080\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserLoadEvent.class, deserialized.getClass());

    BrowserLoadEvent event = (BrowserLoadEvent) deserialized;
    assertEquals("http://localhost:8080", event.arguments.getLocation());
    assertEquals(BrowserEventType.LOAD, event.getEventType());
  }

  @Test
  public void unloadBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 1, \"a\": [\"http://localhost:8080\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserUnloadEvent.class, deserialized.getClass());

    BrowserUnloadEvent event = (BrowserUnloadEvent) deserialized;
    assertEquals("http://localhost:8080", event.arguments.getLocation());
    assertEquals(BrowserEventType.UNLOAD, event.getEventType());
  }

  @Test
  public void resizeBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 2, \"a\": [100, 200]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserResizeEvent.class, deserialized.getClass());

    BrowserResizeEvent event = (BrowserResizeEvent) deserialized;
    assertEquals(100, event.arguments.getInnerWidth());
    assertEquals(200, event.arguments.getInnerHeight());
    assertEquals(BrowserEventType.RESIZE, event.getEventType());
  }

  @Test
  public void performanceMeasureBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\": 13097,\"e\": 3,\"a\": [\"⚛ FormControl [update]\", \"measure\", 18549.754999927245, 1.9050000701099634]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserPerformanceEvent.class, deserialized.getClass());

    BrowserPerformanceEvent event = (BrowserPerformanceEvent) deserialized;
    assertEquals("⚛ FormControl [update]", event.arguments.getName());
    assertEquals("measure", event.arguments.getEntryType());
    assertEquals(18549.754999927245, event.arguments.getStartTime());
    assertEquals(1.9050000701099634, event.arguments.getDuration());
    assertEquals(BrowserEventType.PERFORMANCE, event.getEventType());
  }

  @Test
  public void performanceNavigationBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\": 17,\"e\": 3,\"a\": [\"http://localhost:3002/\", \"navigation\", 0, 5478.304999996908]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserPerformanceEvent.class, deserialized.getClass());

    BrowserPerformanceEvent event = (BrowserPerformanceEvent) deserialized;
    assertEquals("http://localhost:3002/", event.arguments.getName());
    assertEquals("navigation", event.arguments.getEntryType());
    assertEquals(0, event.arguments.getStartTime());
    assertEquals(5478.304999996908, event.arguments.getDuration());
    assertEquals(BrowserEventType.PERFORMANCE, event.getEventType());
  }

  @Test
  public void navigateBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\": 1234, \"e\": 0, \"a\": [\"http://localhost:8080/test\", \"Test title\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserNavigateEvent.class, deserialized.getClass());

    BrowserNavigateEvent event = (BrowserNavigateEvent) deserialized;
    assertEquals("http://localhost:8080/test", event.arguments.getLocation());
    assertEquals("Test title", event.arguments.getTitle());
    assertEquals(BrowserEventType.NAVIGATE, event.getEventType());
  }

  @Test
  public void fetchBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\":520066,\"e\":11,\"a\":[\"POST\",\"http://localhost:8081/v1/beacon/beat?organizationId=test-1&sessionId=912969ea-0f2e-473d-b1b6-473b9787c3e0&deviceId=51c383f8-8654-4f66-a0d6-a6cd73893ec4&pageId=7c6aaeed-b23b-484b-9d5d-4c680b4e2b93\",422,\"cors\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserFetchEvent.class, deserialized.getClass());

    BrowserFetchEvent event = (BrowserFetchEvent) deserialized;
    assertEquals("POST", event.arguments.getMethod());
    assertEquals(
        "http://localhost:8081/v1/beacon/beat?organizationId=test-1&sessionId=912969ea-0f2e-473d-b1b6-473b9787c3e0&deviceId=51c383f8-8654-4f66-a0d6-a6cd73893ec4&pageId=7c6aaeed-b23b-484b-9d5d-4c680b4e2b93",
        event.arguments.getUrl());
    assertEquals(422, event.arguments.getStatus());
    assertEquals("cors", event.arguments.getType());
    assertEquals(BrowserEventType.FETCH, event.getEventType());
  }

  @Test
  public void clickNodeBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\": 1306,\"e\": 4,\"a\": [1167, 732, \"<BUTTON\", \":data-baseweb\", \"button\", \":type\", \"submit\", \":class\", \"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserClickEvent.class, deserialized.getClass());

    BrowserClickEvent event = (BrowserClickEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    assertEquals(BrowserEventType.CLICK, event.getEventType());
  }

  @Test
  public void clickBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload = "{\"t\": 1306,\"e\": 4,\"a\": [1167, 732]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserClickEvent.class, deserialized.getClass());

    BrowserClickEvent event = (BrowserClickEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.empty(), event.getNode());
    assertEquals(Collections.emptyList(), event.getNodeWithAttributes());
    assertEquals(BrowserEventType.CLICK, event.getEventType());
  }

  @Test
  public void mouseMoveBeaconEventDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\": 1306,\"e\": 5,\"a\": [1167, 732, \"<BUTTON\", \":data-baseweb\", \"button\", \":type\", \"submit\", \":class\", \"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserMouseMoveEvent.class, deserialized.getClass());

    BrowserMouseMoveEvent event = (BrowserMouseMoveEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    assertEquals(BrowserEventType.MOUSEMOVE, event.getEventType());
  }

  @Test
  public void logEventBeaconDeserializationTest() throws JsonProcessingException {
    String payload = "{\"t\": 10812,\"e\": 9,\"a\": [\"error\",\"HAHA\"]}";
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserLogEvent.class, deserialized.getClass());

    BrowserLogEvent event = (BrowserLogEvent) deserialized;
    assertEquals(LogLevel.ERROR, event.getLevel());
    assertEquals(List.of("HAHA"), event.getArguments());
    assertEquals(BrowserEventType.LOG, event.getEventType());
  }

  @Test
  public void errorEventBeaconDeserializationTest() throws JsonProcessingException {
    String payload =
        "{\"t\":10158.850000007078,\"e\":10,\"a\":[\"Unexpected identifier\",\"SyntaxError\",\"SyntaxError: Unexpected identifier\"]}";

    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserErrorEvent.class, deserialized.getClass());

    BrowserErrorEvent event = (BrowserErrorEvent) deserialized;
    assertEquals("SyntaxError", event.getArguments().getName());
    assertEquals("Unexpected identifier", event.getArguments().getMessage());
    assertEquals(10158.0, event.getTimestamp());
    assertTrue(event.getArguments().getStack().contains("SyntaxError: Unexpected identifier"));
  }
}