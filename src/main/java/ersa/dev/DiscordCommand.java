package ersa.dev;

import dev.ersa.api.commands.base.ErsaCommand;
import dev.ersa.api.commands.base.context.CommandContext;

import java.util.List;

public final class DiscordCommand extends ErsaCommand {
    private final ErsaCommandListener listener;

    public DiscordCommand(ErsaCommandListener listener) {
        super("discord");
        this.listener = listener;
    }

    @Override
    public String getDescription() {
        return "Reload ErsaDiscord config";
    }

    @Override
    public String getUsage() {
        return "/ersa discord reload";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean noConsole() {
        return false;
    }

    @Override
    public boolean onCommand(CommandContext context) {
        String arg;
        try {
            arg = context.getString(0);
        } catch (Exception e) {
            context.getCommandSender().sendMessage("Usage: /ersa discord reload");
            return true;
        }
        if (!arg.equalsIgnoreCase("reload")) {
            context.getCommandSender().sendMessage("Usage: /ersa discord reload");
            return true;
        }
        boolean ok = listener.reload();
        if (ok) {
            context.getCommandSender().sendMessage("ErsaDiscord config reloaded.");
        } else {
            context.getCommandSender().sendMessage("ErsaDiscord reloaded (alerts disabled or webhook invalid, see console).");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandContext context) {
        return context.getArgs().length <= 1 ? List.of("reload") : List.of();
    }
}
