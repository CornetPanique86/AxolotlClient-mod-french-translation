package io.github.moehreag.axolotlclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.moehreag.axolotlclient.Axolotlclient;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class ConfigHandler {

    //Based around https://github.com/PseudoDistant/AnTitanic/blob/1.8.9/src/main/java/io/github/farlandercraft/antitanic/AnTitanic.java

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static Path confPath = FabricLoader.getInstance().getConfigDir().resolve("Axolotlclient.json");

    public static void init() {
        if(!Files.exists(confPath)) {

            Axolotlclient.CONFIG = new AxolotlclientConfig(
                    new AxolotlclientConfig.nametagConf(
                            false,
                            false
                    ),
                    new AxolotlclientConfig.Badges(
                            true,
                            false,
                            ""
                    ),
                    new AxolotlclientConfig.nh(
                            false,
                            "",
                            false,
                            "Player",
                            false,
                            false
                    ),
                    new AxolotlclientConfig.other(),
                    new AxolotlclientConfig.rpcConfig(
                            true,
                            true
                    )
            );
            save();

        } else {
            try {
                Axolotlclient.CONFIG = gson.fromJson(new String(Files.readAllBytes(confPath)), AxolotlclientConfig.class);
            } catch (Exception ignored){}
        }
    }


    public static void save(){

        try {
            Files.write(confPath, Collections.singleton(gson.toJson(Axolotlclient.CONFIG)));
        } catch (Exception ignored){}

    }

}
