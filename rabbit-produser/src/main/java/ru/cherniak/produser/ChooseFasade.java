package ru.cherniak.produser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ChooseFasade {

  private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

  public void sendMessageToConsole(String request, List<String> content) {
    System.out.println(request);
    for (int i = 0; i < content.size(); i++) {
      int k = i + 1;
      System.out.printf("%d - %s%n", k, content.get(i));
    }
  }

  public String readStringFromConsole() throws IOException {
    return reader.readLine().trim();
  }

  public boolean isOutOfBounds(int indexL, int indexS, int limitL, int limitS) {
    if (indexL < 1 || indexL > limitL
        || indexS < 1 || indexS > limitS) {
      System.out.println("Введено недопустимое значение");
      return true;
    }
    return false;
  }
}
