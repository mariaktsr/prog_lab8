package client.gui.service;

import client.gui.canvas.CanvasRenderer;
import client.network.NetworkClient;
import common.commands.CommandType;
import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CollectionSyncService {

    private final NetworkClient networkClient;
    private final User currentUser;
    private final ObservableList<HumanBeing> tableData;
    private final CanvasRenderer canvasRenderer;
    private final ScheduledExecutorService scheduler;
    private List<HumanBeing> lastKnownCollection;

    public CollectionSyncService(NetworkClient networkClient, User currentUser,
                                 ObservableList<HumanBeing> tableData,
                                 CanvasRenderer canvasRenderer) {
        this.networkClient = networkClient;
        this.currentUser = currentUser;
        this.tableData = tableData;
        this.canvasRenderer = canvasRenderer;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CollectionSyncService");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(int delaySeconds, int periodSeconds) {
        scheduler.scheduleAtFixedRate(this::syncCollection, delaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void syncCollection() {
        if (currentUser == null || networkClient == null) return;

        try {
            Request req = new Request(CommandType.SHOW, new String[0], null, currentUser);
            Response resp = networkClient.sendRequest(req);

            if (resp.isSuccess() && resp.getData(List.class) != null) {
                List<HumanBeing> newCollection = ((List<?>) resp.getData(List.class)).stream()
                        .filter(o -> o instanceof HumanBeing)
                        .map(o -> (HumanBeing) o)
                        .collect(Collectors.toList());

                if (hasCollectionChanged(newCollection)) {
                    lastKnownCollection = newCollection;
                    Platform.runLater(() -> {
                        tableData.setAll(newCollection);
                        canvasRenderer.syncData(newCollection);
                    });
                }
            }
        } catch (Exception e) {
            // Тихо игнорируем ошибки
        }
    }

    private boolean hasCollectionChanged(List<HumanBeing> newCollection) {
        if (lastKnownCollection == null) return true;
        if (lastKnownCollection.size() != newCollection.size()) return true;

        long oldHash = lastKnownCollection.stream().mapToLong(HumanBeing::getId).sum();
        long newHash = newCollection.stream().mapToLong(HumanBeing::getId).sum();
        return oldHash != newHash;
    }
}