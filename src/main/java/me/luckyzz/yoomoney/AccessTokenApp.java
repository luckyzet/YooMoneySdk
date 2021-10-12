package me.luckyzz.yoomoney;

import com.yoo.money.api.authorization.AuthorizationData;
import com.yoo.money.api.authorization.AuthorizationParameters;
import com.yoo.money.api.methods.Token;
import com.yoo.money.api.model.Scope;
import com.yoo.money.api.net.AuthorizationCodeResponse;
import com.yoo.money.api.net.clients.ApiClient;
import com.yoo.money.api.net.clients.DefaultApiClient;
import com.yoo.money.api.util.Language;

import java.io.File;
import java.util.Scanner;

public final class AccessTokenApp {

    public static void main(String[] args) {
        try {

            // 4100116293723438.E661E9735DA3151096E973FB8C6021FB451582481BD5D96A3782139B03E8B4DA82836F8F6B64DFF32F64FD0126EAA1BCF1DE0DD2A084153B3A0A7F17DBE648DD961BAB5BD19F422E3E5127DABCA6C371B0C847A225682BEB6E5A1FDF5E0C1E666C517919FF7F7A97F12C7EB8A13F5D47303F99C4D4E84A470F11D796E46E3659

            Scanner scanner = new Scanner(System.in);

            System.out.println("Введите clientId Вашего приложения");
            String clientId = scanner.next();

            System.out.println("Введите clientSecret Вашего приложения");
            String clientSecret = scanner.next();

            ApiClient client = new DefaultApiClient.Builder()
                    .setClientId(clientId)
                    .setLanguage(Language.RUSSIAN)
                    .create();

            AuthorizationParameters parameters = new AuthorizationParameters.Builder()
                    .setInstanceName("smsbot_test")
                    .setResponseType("code")
                    .setRedirectUri("https://t.me/SMSBOT_test_bot")
                    .addScope(Scope.ACCOUNT_INFO)
                    .addScope(Scope.OPERATION_HISTORY)
                    .addScope(Scope.OPERATION_DETAILS)
                    .addScope(Scope.INCOMING_TRANSFERS)
                    .addScope(Scope.PAYMENT_P2P)
                    .addScope(Scope.PAYMENT_SHOP)
                    .create();
            AuthorizationData data = client.createAuthorizationData(parameters);
            System.out.println(data.getUrl());

            System.out.println("Отправьте ссылку, на которую Вас перебросило после выдачи доступа к аккаунту (телеграмм сайт там)");
            AuthorizationCodeResponse response = AuthorizationCodeResponse.parse(scanner.next());

            if (response.error == null) {
                Token token = client.execute(new Token.Request(response.code, client.getClientId(), "https://t.me/SMSBOT_test_bot",
                        clientSecret));
                if (token.error == null) {
                    client.setAccessToken(token.accessToken);

                    System.out.println("Access token, который Вы должны установить в конфиг - " + token.accessToken);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
