package server.handler.command;

import common.request.Request;
import common.response.Response;

public interface Command {
    Response execute(Request request);
    String getDescription();
}