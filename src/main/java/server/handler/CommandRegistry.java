package server.handler;

import common.commands.CommandType;
import common.model.CommandDescriptor;
import common.model.ValidatorConfig;
import server.db.DatabaseManager;
import server.handler.command.Command;
import server.handler.command.CommandInvoker;
import server.handler.command.complex.AddCommand;
import server.handler.command.complex.InsertAtCommand;
import server.handler.command.complex.UpdateCommand;
import server.handler.command.noarg.*;
import server.handler.command.onearg.ExecuteScriptCommand;
import server.handler.command.onearg.FilterContainsNameCommand;
import server.handler.command.onearg.RemoveAtCommand;
import server.handler.command.onearg.RemoveByIdCommand;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {

    // Внутренняя запись: связывает метаданные и объект команды
    private static class CommandEntry {
        final CommandDescriptor descriptor;
        final Command command;
        CommandEntry(CommandDescriptor descriptor, Command command) {
            this.descriptor = descriptor;
            this.command = command;
        }
    }

    private final Map<CommandType, CommandEntry> registry = new EnumMap<>(CommandType.class);
    private final ICollectionManager cm;
    private final DatabaseManager dbm;

    //Конструктор принимает ICollectionManager (интерфейс) вместо конкретного класса
    public CommandRegistry(ICollectionManager cm, DatabaseManager dbm) {
        this.cm = cm;
        this.dbm = dbm;
        registerDefaults();
    }

    private void registerDefaults() {
        //No-arg
        register(CommandType.HELP, new HelpCommand(this::getAllCommands), false, false, 0, List.of(ValidatorConfig.argumentCount("help", 0)));
        register(CommandType.INFO, new InfoCommand(cm), false, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.SHOW, new ShowCommand(cm), false, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.CLEAR, new ClearCommand(cm, dbm), false, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.SORT, new SortCommand(cm), false, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.SUM_OF_IMPACT_SPEED, new SumOfImpactSpeedCommand(cm), false, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.PRINT_FIELD_DESCENDING_MOOD, new PrintFieldDescendingMoodCommand(cm), false, false, 0, List.of(ValidatorConfig.noOp()));

        //One-arg
        register(CommandType.REMOVE_BY_ID, new RemoveByIdCommand(cm, dbm), false, false, 1, List.of(
                ValidatorConfig.argumentCount("remove_by_id", 1),
                ValidatorConfig.notEmpty(0),
                ValidatorConfig.numeric(0)
        ));
        register(CommandType.REMOVE_AT, new RemoveAtCommand(cm, dbm), false, false, 1, List.of(
                ValidatorConfig.argumentCount("remove_at", 1),
                ValidatorConfig.notEmpty(0),
                ValidatorConfig.numeric(0)
        ));
        register(CommandType.FILTER_CONTAINS_NAME, new FilterContainsNameCommand(cm), false, false, 1, List.of(
                ValidatorConfig.argumentCount("filter_contains_name", 1),
                ValidatorConfig.notEmpty(0)
        ));
        register(CommandType.EXECUTE_SCRIPT, new ExecuteScriptCommand(), false, false, 1, List.of(
                ValidatorConfig.argumentCount("execute_script", 1),
                ValidatorConfig.notEmpty(0)
        ));

        //Complex
        register(CommandType.ADD, new AddCommand(cm, dbm), true, false, 0, List.of(ValidatorConfig.noOp()));
        register(CommandType.UPDATE, new UpdateCommand(cm, dbm), true, false, 1, List.of(
                ValidatorConfig.argumentCount("update", 1),
                ValidatorConfig.numeric(0)
        ));
        register(CommandType.INSERT_AT, new InsertAtCommand(cm, dbm), true, false, 1, List.of(
                ValidatorConfig.argumentCount("insert_at", 1),
                ValidatorConfig.numeric(0)
        ));

        //Server-only
        register(CommandType.SAVE_SERVER, new SaveServerCommand(cm, dbm), false, true, 0, List.of(ValidatorConfig.noOp()));

        //Внутренняя (не отдаётся клиенту, не регистрируется в Invoker)
        registry.put(CommandType.GET_COMMANDS_METADATA,
                new CommandEntry(new CommandDescriptor("get_commands_metadata", false, false, 0, List.of(ValidatorConfig.noOp())), null));
    }

    private void register(CommandType type, Command cmd, boolean requiresData, boolean serverOnly,
                          int argCount, List<ValidatorConfig> validators) {
        registry.put(type, new CommandEntry(
                new CommandDescriptor(type.name().toLowerCase(), requiresData, serverOnly, argCount, validators),
                cmd
        ));
    }

    //Для рукопожатия: отдаёт только клиентские команды
    public Map<String, CommandDescriptor> getClientDescriptors() {
        Map<String, CommandDescriptor> result = new HashMap<>();
        for (Map.Entry<CommandType, CommandEntry> e : registry.entrySet()) {
            if (!e.getValue().descriptor.isServerOnly() && e.getKey() != CommandType.GET_COMMANDS_METADATA) {
                result.put(e.getKey().name().toLowerCase(), e.getValue().descriptor);
            }
        }
        return result;
    }

    //Создаёт CommandInvoker и заполняет его командами
    public CommandInvoker createInvoker() {
        CommandInvoker invoker = new CommandInvoker();
        for (Map.Entry<CommandType, CommandEntry> e : registry.entrySet()) {
            if (e.getValue().command != null) {
                invoker.register(e.getKey(), e.getValue().command);
            }
        }
        return invoker;
    }

    //Проверки для CommandHandler
    public boolean isServerOnly(CommandType type) {
        CommandEntry e = registry.get(type);
        return e != null && e.descriptor.isServerOnly();
    }

    //Для HelpCommand
    public Map<CommandType, Command> getAllCommands() {
        Map<CommandType, Command> map = new HashMap<>();
        for (Map.Entry<CommandType, CommandEntry> e : registry.entrySet()) {
            if (e.getValue().command != null) map.put(e.getKey(), e.getValue().command);
        }
        return map;
    }
}