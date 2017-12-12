package org.zells.dish.zells;

import org.zells.dish.codec.Codec;
import org.zells.dish.codec.impl.FlatByteTreeCodec;
import org.zells.dish.codec.impl.SignalByteSource;
import org.zells.dish.codec.impl.SignalTreeCodec;
import org.zells.dish.core.Dish;
import org.zells.dish.core.Signal;
import org.zells.dish.core.Zell;
import org.zells.dish.core.impl.StandardSignal;

import java.util.*;

public class TurtleZell implements Zell {

  private Codec codec = new FlatByteTreeCodec();
  private SignalTreeCodec translator = new SignalTreeCodec();
  private Dish dish;
  private Map<String, Map> canvases = new HashMap<>();

  private String name;
  private Point position = new Point(0, 0);
  private double angle = 0;

  public TurtleZell(Dish dish, String name) {
    this.dish = dish;
    this.name = name;
  }

  @Override
  public void receive(Signal signal) {
    Object decoded = codec.decode(new SignalByteSource(signal));
    Map<Object, Object> message = translator.asMap(decoded);

    if (!message.containsKey("to") || !message.containsKey("content")) {
      System.out.println("Not a message");
      return;
    }

    if (translator.asString(message.get("to")).equals("?")) {
      transmit(new Dictionary()
          .with("to", message.get("from"))
          .with("from", name)
          .with("content", Collections.singletonList(
              new Dictionary()
                  .with("to", name)
                  .with("from", message.get("from"))
                  .with("content", "?"))));
      return;
    }

    if (!translator.asString(message.get("to")).equals(name)) {
      System.out.println("Not for me");
      return;
    }


    if (translator.asString(message.get("content")).equals("?")) {
      transmit(new Dictionary()
          .with("to", message.get("from"))
          .with("from", name)
          .with("content", Arrays.asList(
              new Dictionary()
                  .with("to", name)
                  .with("content", new Dictionary()
                      .with("draw on", "a display")
                      .with("size", new Dictionary()
                          .with("width", 400)
                          .with("height", 200))),
              new Dictionary()
                  .with("to", name)
                  .with("content", new Dictionary()
                      .with("go", "forward")),
              new Dictionary()
                  .with("to", name)
                  .with("content", new Dictionary()
                      .with("turn", "left")),
              new Dictionary()
                  .with("to", name)
                  .with("content", new Dictionary()
                      .with("turn", "right"))
          )));
    }

    Map<Object, Object> content = translator.asMap(message.get("content"));

    if (content.containsKey("draw on")) {
      canvases.put(translator.asString(content.get("draw on")),
          translator.asMap(content.get("size")));
      draw();
    } else if (content.containsKey("go")) {
      position = position.translated(new Point(0, 10).rotated(angle));
      System.out.println("Position " + position.x + "," + position.y);
      draw();
    } else if (content.containsKey("turn")) {
      if (translator.asString(content.get("turn")).equals("left")) {
        angle += 15;
      } else {
        angle -= 15;
      }
      draw();
    } else if (translator.asString(message.get("content")).equals("reset")) {
      position = new Point(0, 0);
      angle = 0;
      draw();
    }
  }

  private void draw() {
    for (String canvas : canvases.keySet()) {
      ArrayList<Point> points = new ArrayList<>();
      points.add(position.translated(new Point(-10, 0).rotated(angle)));
      points.add(position.translated(new Point(10, 0).rotated(angle)));
      points.add(position.translated(new Point(0, 20).rotated(angle)));

      ArrayList<Line> lines = new ArrayList<>();
      lines.add(new Line(points.get(0), points.get(1)));
      lines.add(new Line(points.get(1), points.get(2)));
      lines.add(new Line(points.get(2), points.get(0)));

      List<Object> strokes = new ArrayList<>();
      strokes.add("clear");

      for (Line l : lines) {
        strokes.add(new Dictionary()
            .with("line", new Dictionary()
                .with("width", 2)
                .with("color", new Dictionary().with("r", 0).with("g", 1).with("b", 0))
                .with("from", new Dictionary().with("x", l.from.x).with("y", l.from.y))
                .with("to", new Dictionary().with("x", l.to.x).with("y", l.to.y))));
      }

      transmit(new Dictionary()
          .with("to", canvas)
          .with("from", name)
          .with("content", new Dictionary()
              .with("draw", new Dictionary()
                  .with("strokes", strokes
                  ))
          ));
    }
  }

  private void transmit(Object signal) {
    dish.transmit(StandardSignal.from(codec.encode(translator.translate(signal))));
  }

  private class Dictionary extends HashMap<Object, Object> {
    Dictionary with(Object key, Object value) {
      put(key, value);
      return this;
    }
  }

  private class Point {
    double x;
    double y;

    Point(double x, double y) {
      this.x = Math.round(x * 100) / 100;
      this.y = Math.round(y * 100) / 100;
    }

    Point translated(Point d) {
      return new Point(x + d.x, y + d.y);
    }

    Point rotated(double angle) {
      double radians = Math.toRadians(angle);
      return new Point(
          x * Math.cos(radians) - y * Math.sin(radians),
          y * Math.cos(radians) + x * Math.sin(radians));
    }
  }

  private class Line {
    Point from;
    Point to;

    Line(Point from, Point to) {
      this.from = from;
      this.to = to;
    }
  }
}
