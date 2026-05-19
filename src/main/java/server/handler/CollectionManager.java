package server.handler;

import common.model.HumanBeing;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CollectionManager implements ICollectionManager {

    private final CopyOnWriteArrayList<HumanBeing> collection;
    private final String initializationDate;

    public CollectionManager() {
        this.collection = new CopyOnWriteArrayList<>();
        this.initializationDate = ZonedDateTime.now().toString();
    }

    @Override public void loadFromDatabase(List<HumanBeing> data) {
        collection.clear(); collection.addAll(data);
    }
    @Override public void add(HumanBeing h) {
        collection.add(h);
    }
    @Override public void insertAt(int index, HumanBeing h) {
        collection.add(index, h);
    }
    @Override public void clear() {
        collection.clear();
    }
    @Override public boolean update(Long id, HumanBeing updated) {
        return collection.stream().filter(h -> h.getId().equals(id))
                .findFirst().map(existing -> { collection.set(collection.indexOf(existing), updated); return true; })
                .orElse(false);
    }
    @Override public boolean removeById(Long id) {
        return collection.removeIf(h -> h.getId().equals(id));
    }
    @Override public void removeAt(int index) {
        if (index >= 0 && index < collection.size()) collection.remove(index);
    }
    @Override public List<HumanBeing> getCollection() {
        return new ArrayList<>(collection);
    }
    @Override public String getInfo() {
        return String.format("Тип коллекции: %s%nДата инициализации: %s%nКоличество элементов: %d",
                collection.getClass().getSimpleName(), initializationDate, collection.size());
    }
    @Override public String getShow() {
        return collection.stream()
                .sorted(Comparator.comparing(HumanBeing::getName))
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n"));
    }
    @Override public String getSortedCollection() {
        return collection.stream()
                .sorted(Comparator.comparing(HumanBeing::getName))
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n"));
    }
    @Override public String getSumOfImpactSpeed() {
        long sum = collection.stream().mapToLong(HumanBeing::getImpactSpeed).sum();
        return "Сумма значений поля impactSpeed: " + sum;
    }
    @Override public String getDescendingMoods() {
        return collection.stream()
                .sorted(Comparator.comparing(HumanBeing::getMood, Comparator.reverseOrder()))
                .map(h -> h.getMood().name())
                .collect(Collectors.joining("\n"));
    }
    @Override public List<HumanBeing> filterContainsName(String substring) {
        return collection.stream()
                .filter(h -> h.getName().contains(substring))
                .collect(Collectors.toList());
    }
    @Override public boolean isOwner(Long id, String login) {
        return collection.stream().filter(h -> h.getId().equals(id))
                .anyMatch(h -> h.getOwner() != null && h.getOwner().getLogin().equals(login));
    }
}