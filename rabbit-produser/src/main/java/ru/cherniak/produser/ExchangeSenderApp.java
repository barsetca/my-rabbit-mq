package ru.cherniak.produser;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;

public class ExchangeSenderApp {

  private static final String EXCHANGE_NAME = "myTopicExchanger";
  private static final String CHOOSE_PROGRAMMING_LANGUAGE =
      "Введите цифру соответвующий языку программирования статьи";
  private static final String CHOOSE_PROGRAMMING_SECTION =
      "Введите цифру соответвующую разделу, выбранного языка статьи";
  private static final String ERROR =
      "Щшибка! Введена несуществующая цифра! Попробуйте снова.";
  private static final List<String> PROGRAMMING_LANGUAGE = new ArrayList<>();
  private static final List<String> PROGRAMMING_LANGUAGE_SECTION = new ArrayList<>();

  static {
    PROGRAMMING_LANGUAGE.add("java");
    PROGRAMMING_LANGUAGE.add("js");
    PROGRAMMING_LANGUAGE.add("html");

    PROGRAMMING_LANGUAGE_SECTION.add("common");
    PROGRAMMING_LANGUAGE_SECTION.add("backend");
    PROGRAMMING_LANGUAGE_SECTION.add("frontend");
  }

  public static void main(String[] argv) throws Exception {
    ChooseFasade chooseFacade = new ChooseFasade();
    while (true) {
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_LANGUAGE, PROGRAMMING_LANGUAGE);
      String langIndex = chooseFacade.readStringFromConsole();
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_SECTION, PROGRAMMING_LANGUAGE_SECTION);
      String sectionIndex = chooseFacade.readStringFromConsole();
      int indexL;
      int indexS;
      try {
        indexL = Integer.parseInt(langIndex);
        indexS = Integer.parseInt(sectionIndex);
        if (
            chooseFacade.isOutOfBounds(
                indexL, indexS, PROGRAMMING_LANGUAGE.size(), PROGRAMMING_LANGUAGE_SECTION.size())
        ) {
          continue;
        }
      } catch (NumberFormatException e) {
        System.out.println(ERROR);
        continue;
      }
      String theme = getTheme(indexL, indexS);
      System.out.println("Введите полный путь файла для публикации используя разделитель /");
      String filePath = chooseFacade.readStringFromConsole();
      publishArticle(theme, filePath);
    }
  }

  private static void publishArticle(String theme, String filePath) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

      channel.basicPublish(EXCHANGE_NAME, theme, null,
          SerializationUtils.serialize(new MyMessage(Files.readAllLines(Paths.get(filePath)))));
      System.out.println(" [x] Sent '" + theme + "'");
      System.out.println(" [x] Sent '" + filePath + "'");
    }
  }

  public static String getTheme(int indexL, int indexS) {
    String lang = PROGRAMMING_LANGUAGE.get(indexL - 1);
    String section = PROGRAMMING_LANGUAGE_SECTION.get(indexS - 1);
    StringBuilder sb = new StringBuilder();
    sb.append("programming.").append(section).append(".").append(lang);
    return sb.toString();
  }
}