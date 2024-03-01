package dev.tinelix.jabwave.telegram.api;


import android.os.Message;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import androidx.annotation.NonNull;
import dev.tinelix.jabwave.JabwaveApp;

public class TelegramClient implements Client.ResultHandler, Client.ExceptionHandler {

   private final Client client;
   private TdApi.TdlibParameters params;

   public TelegramClient() {
      this.params = new TdApi.TdlibParameters();
      this.client = Client.create(this, null ,this);
   }

   public TelegramClient(TdApi.TdlibParameters params) {
      this.params = params;
      this.client = Client.create(this, null ,this);
   }

   @Override
   public void onResult(TdApi.Object object) {
      Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
              String.format("[RESULT] Class %s", object.getClass().getSimpleName())
      );
   }

   @Override
   public void onException(Throwable e) {
      Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
              String.format("[ERROR] %s: %s", e.getClass().getSimpleName(), e.getMessage())
      );
   }

   @Override
   public boolean handleMessage(@NonNull Message msg) {
      return false;
   }
}
