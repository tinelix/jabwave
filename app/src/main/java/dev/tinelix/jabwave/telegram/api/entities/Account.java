package dev.tinelix.jabwave.telegram.api.entities;

import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class Account extends dev.tinelix.jabwave.core.ui.list.items.base.Account {
    private TDLibClient client;
    public String username;
    private String phone_number;
    public String about;

    public Account(long id, String first_name, String last_name) {
        super(id, first_name, last_name);
    }

    public Account(TDLibClient client, TDLibClient.ApiHandler handler) {
        super();
        this.client = client;
        client.send(new TdApi.GetMe(), new TDLibClient.ApiHandler() {
            @Override
            public void onSuccess(TdApi.Function function, TdApi.Object object) {
                if(object instanceof TdApi.User) {
                    TdApi.User user = ((TdApi.User) object);
                    id = user.id;
                    first_name = user.firstName;
                    last_name = user.lastName;
                    username = user.username;
                    phone_number = user.phoneNumber;
                    if(user.profilePhoto != null) {
                        if(user.profilePhoto.small.local.isDownloadingCompleted)
                            loadPhoto(photo_small, new File(user.profilePhoto.small.local.path));
                        if(user.profilePhoto.big.local.isDownloadingCompleted)
                            loadPhoto(photo, new File(user.profilePhoto.big.local.path));
                    }
                    handler.onSuccess(function, object);
                }
            }

            @Override
            public void onFail(TdApi.Function function, Throwable throwable) {

            }
        });
    }

    private void loadPhoto(byte[] bytes, File file) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPhoneNumber() {
        return phone_number;
    }
}
