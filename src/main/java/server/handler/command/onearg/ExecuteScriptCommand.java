package server.handler.command.onearg;

import common.request.Request;
import common.response.Response;
import server.handler.command.Command;

//Команда выполнения скрипта
//(на сервере не выполняется, так как требует интерактивного ввода данных)

public class ExecuteScriptCommand implements Command {

    @Override
    public Response execute(Request request) {
        return Response.error(
                "Команда 'execute_script' должна выполняться на клиенте. " +
                        "Сервер не может запрашивать данные у пользователя через консоль."
        );
    }

    @Override
    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла (выполняется на клиенте)";
    }
}