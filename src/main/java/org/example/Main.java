package org.example;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {

    private CommandVisibility visibility;
    private GameRuleSync gameRules;
    private Dimensions dimensions;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        Msg.load(this);

        visibility = new CommandVisibility(this);
        getServer().getPluginManager().registerEvents(visibility, this);
        JoinQuitListener joinQuit = new JoinQuitListener(this);
        getServer().getPluginManager().registerEvents(joinQuit, this);
        joinQuit.hook();
        getServer().getPluginManager().registerEvents(new SakuraCreepers(this), this);
        getServer().getPluginManager().registerEvents(new ChatStyle(this), this);
        Guide guide = new Guide(this);
        getServer().getPluginManager().registerEvents(guide, this);
        guide.hookAuthMe();
        dimensions = new Dimensions(this);
        getServer().getPluginManager().registerEvents(dimensions, this);
        gameRules = new GameRuleSync(this);
        getServer().getPluginManager().registerEvents(gameRules, this);
        // миры уже загружены к этому моменту — синхронизируем после старта
        getServer().getScheduler().runTask(this, gameRules::syncAll);

        // Команды: имена, алиасы и включение — в config.yml → commands
        Commands.register(this, "spawn", "Teleport to spawn", null, List.of(), new SpawnCommand(this), null);
        Commands.register(this, "home", "Teleport home (to your bed)", null, List.of(), new HomeCommand(this), null);
        TpaCommand tpa = new TpaCommand(this);
        Commands.register(this, "tpa", "Ask a player to teleport to them", null, List.of(), tpa, tpa);
        Commands.register(this, "tpaaccept", "Accept a teleport request", null, List.of("tpaccept", "tpyes"), new TpaAcceptCommand(this), (s, c, l, a) -> List.of());
        Commands.register(this, "tpadeny", "Deny a teleport request", null, List.of("tpno"), new TpaDenyCommand(this), (s, c, l, a) -> List.of());
        Commands.register(this, "resources", "Teleport to the resource world", null, List.of("res", "ресурсы"), new ResourcesCommand(this), (s, c, l, a) -> List.of());
        WhisperCommand msg = new WhisperCommand(this, false);
        Commands.register(this, "msg", "Private message", null, List.of("tell", "w", "whisper", "m", "pm"), msg, msg);
        WhisperCommand reply = new WhisperCommand(this, true);
        Commands.register(this, "reply", "Reply to the last private message", null, List.of("r"), reply, reply);

        Commands.register(this, "guide", "The Hanafubuki guide book", null, List.of("rules", "правила", "книга"),
                guide, (snd, c, l, a) -> List.of());
        Commands.register(this, "admin", "Hanafubuki admin: reload, version", "hanafubuki.admin", List.of("hf"),
                new AdminCommand(this), new AdminCommand(this));

        visibility.setHelp(new Help(this, visibility));

        getLogger().info("Plugin enabled");
    }

    /** /hanafubuki reload: конфиг, тексты, стиль, списки скрытых команд. Имена команд — только после рестарта. */
    public void reloadAll() {
        reloadConfig();
        Msg.load(this);
        visibility.reload();
        gameRules.syncAll();
        for (var p : getServer().getOnlinePlayers()) p.updateCommands();
    }

    public Dimensions dimensions() {
        return dimensions;
    }
}
