package com.telegram.OurWishlistBot.model;

import com.telegram.OurWishlistBot.repo.GiftRepository;
import com.telegram.OurWishlistBot.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;


@Component
public class Bot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GiftRepository giftRepository;
    private static final String START = "/start";
    private static final String CANSEL = "/cansel";
    private static final String ADD_USER = "/add_user";
    private static final String DELETE_USER = "/del_user";
    private static final String MY_WISHLIST = "/my_wishlist";
    private static final String WISHLISTS = "/wishlists";
    private static final String ADD_GIFT = "/add";
    private static final String DELETE_GIFT= "/delete";
    private static final Long adminId = Long.valueOf("${admin.id}");
    private static final String cansel = "\n\n/cansel - Нажми для отмены";
    private static final Map<User, Status> userStatusMap = new HashMap<>();
    private static List<User> waitNewUser;
    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            sendError(chatId);
            return;
        }
        User user = optionalUser.get();
        String command = update.getMessage().getText();
        switch (command) {
            case START -> startCommand(chatId, user); break;
            case CANSEL -> canselCommand(chatId, user); break;
            case ADD_USER -> {
                if (isAdmin(user)) {
                    addUserCommand(chatId, user);
                } else {
                    sendError(chatId);
                }
            }
            case DELETE_USER -> {
                if (isAdmin(user)) {
                    deleteUserCommand(chatId, user);
                } else {
                    sendError(chatId);
                }
            }
            case WISHLISTS -> wishlistsCommand(chatId, user); break;
            case MY_WISHLIST -> myWishlistsCommand(chatId, user); break;
            default -> {
                if (userStatusMap.containsKey(user)) {
                    routMessage(chatId, user, command);
                } else {
                    sendError(chatId);
                }
            }
        }

    }
    private void startCommand(Long chatId, User user) {
        String string = """
                        Привет, %s, добро пожаловать!
                        
                        Если ты хочешь добавить подарок в свой WISHLIST,
                        то, просто отправь мне его описание!
                        
                        Если же ты хочешь увидеть свой или чужой WISHLIST
                        просто нажми сюда:
                        %s - Посмотреть вишлисты
                        %s - Редактировать свой вишлист
                        """;

        if (isAdmin(user)) {
            string += string + String.format("\n %s - Добавить нового пользователя"
                                            + "\n %s - Удалить пользователя", ADD_USER, DELETE_USER);
        }
        String answer = String.format(string, user.getName(), WISHLISTS, MY_WISHLIST);
        sendMessage(chatId, answer);
    }
    private void canselCommand(Long chatId, User user) {
        userStatusMap.remove(user);
        startCommand(chatId, user);
    }
    private void addUserCommand(Long chatId, User user) {
        sendMessage(chatId,"Для того, чтобы добавить пользователя введи его имя и ID через пробел" + cansel);
        userStatusMap.put(user, Status.NEW_USER);
    }
    private void deleteUserCommand(Long chatId, User user) {
        StringBuilder answer = new StringBuilder();
        Iterable<User> users = userRepository.findAll();
        for (User u : users) {
            answer.append(String.format("%d. %s \n", u.getId(), u.getName()));
        }
        answer.append("Для того, чтобы удалить пользователя введи его номер %s" + cansel);
        userStatusMap.put(user, Status.DELETE_USER);
        sendMessage(chatId, answer.toString());

    }
    private void wishlistsCommand(Long chatId, User user) {
        String answer = "Выбери чей вишлист ты хочешь посмотреть и пришли мне его номер :) \n";
        userStatusMap.put(user, Status.CHOOSE_GIFT);
        sendMessage(chatId, answer + getWishlist(user) + cansel);
    }
    private void myWishlistsCommand(Long chatId, User user) {
        StringBuilder answer = new StringBuilder("Привет, вот твой вишлист:\n");
        answer.append(getWishlist(user));
        answer.append(String.format("%s - Добавить подарок\n", ADD_GIFT));
        answer.append(String.format("%s - Удалить подарок", DELETE_GIFT));
        sendMessage(chatId, answer.toString());
    }
    private void addGiftCommand(Long chatId, User user, String description) {
        userStatusMap.put(user, Status.NEW_GIFT);
        sendMessage(chatId, "Что хочешь получить в подарок?" + cansel);
    }
    private void deleteGiftCommand(Long chatId, User user) {
        userStatusMap.put(user, Status.DELETE_GIFT);
        sendMessage(chatId, "Введи номер подарка, который хочешь удалить" + cansel);
    }
    private String getListOfUser(User user) {
        StringBuilder list = new StringBuilder();
        for (User u : userRepository.findAll()) {
            if (!u.getId().equals(user.getId())){
                list.append(String.format("%d. %s \n", u.getId(), u.getName());
            }
        }
        return list.toString();
    }
    private String getWishlist(User user) {
        StringBuilder list = new StringBuilder();
        List<Gift> gifts = giftRepository.getGiftsByUserID(user.getUserId());
        for (Gift gift : gifts) {
            list.append(String.format("%d. %s \n", gift.getId(), gift.getDescription()));
        }
        return list.toString();
    }
    private void sendError(Long chatId) {
        sendMessage(chatId, "Упс, что-то пошло не так" + cansel);
    }
    private void sendMessage(Long chatId, String answer) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), answer);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            userStatusMap.clear();
            System.out.println("Ошибка отправки сообщения");
        }
    }
    private void routMessage(Long chatId, User user, String message) {
        switch (userStatusMap.get(user)) {
            case NEW_USER -> {
                if (isAdmin(user)) {
                    addUser(chatId, message);
                } else {
                    sendError(chatId);
                }
            }
            case DELETE_USER -> {
                if (isAdmin(user)) {
                    deleteUser(chatId, message);
                } else {
                    sendError(chatId);
                }
            }
            case NEW_GIFT -> addGift(chatId, user, message);
            case DELETE_GIFT -> deleteGift(chatId, user, message);
            case CHOOSE_GIFT -> getUserWishlist(chatId, message);
            default -> sendError(chatId);
        }
        userStatusMap.remove(user);
    }
    private void addUser(Long chatId, String message) {
        String[] info = message.split(" ");
        User user = new User(info[0], Long.valueOf(info[1]));
        userRepository.save(user);
        sendMessage(chatId, "Пользователь добавлен");
    }
    private void deleteUser(Long chatId, String message) {
        userRepository.deleteById(Long.valueOf(message));
        sendMessage(chatId, "Пользователь удален");
    }
    private void getUserWishlist(Long chatId, String message) {
        User user = userRepository.findById(Long.valueOf(message)).get();
        sendMessage(chatId, getWishlist(user));
    }
    private void addGift(Long chatId, User user, String description) {
        Gift gift = new Gift(description, user.getUserId());
        giftRepository.save(gift);
        userStatusMap.remove(user);
        sendMessage(chatId, "Твой вишлист пополнился");
    }
    private void deleteGift(Long chatId, User user, String id) {
        Optional<Gift> optionalGift = giftRepository.findById(Long.valueOf(id));
        if (optionalGift.isEmpty()) {
            sendMessage(chatId, "Кажется ты ввел некорректный номер, попробуй еще раз или отмени операцию"
                    + cansel);
            return;
        }
        Gift gift = optionalGift.get();
        gift.setGiftStatus(GiftStatus.IS_DELETED);
        userStatusMap.remove(user);
        sendMessage(chatId, "Подарок удален!");
    }
    private boolean isAdmin(User user) {
        return user.getId().equals(adminId);
    }

    @Override
    public String getBotUsername() {
        return "${bot.name}";
    }
}
