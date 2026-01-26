package org.example.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.list.LinkedList;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
  @Test
  public void testJoin_EmptyList() {
    String joinedString = StringUtils.join(new LinkedList());
    assertEquals("", joinedString);
  }

  @Test
  public void testJoin_SingleList() {
    LinkedList list = new LinkedList();
    list.add("First element");
    String joinedString = StringUtils.join(list);
    assertEquals("First element", joinedString);
  }

  @Test
  public void testJoin_TwoNodes() {
    LinkedList list = new LinkedList();
    list.add("First element");
    list.add("Second element");
    String joinedString = StringUtils.join(list);
    assertEquals("First element Second element", joinedString);
  }
}
