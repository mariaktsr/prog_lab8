package server.handler;

import common.model.HumanBeing;

import java.util.List;

public class CollectionManagerProxy implements ICollectionManager {

    private final CollectionManager realManager;
    private final String userLogin;

    public CollectionManagerProxy(CollectionManager realManager, String userLogin) {
        this.realManager = realManager;
        this.userLogin = userLogin;
    }

    @Override public void loadFromDatabase(List<HumanBeing> data) {
        realManager.loadFromDatabase(data);
    }
    @Override public void add(HumanBeing h) {
        realManager.add(h);
    }
    @Override public void insertAt(int index, HumanBeing h) {
        realManager.insertAt(index, h);
    }
    @Override public boolean update(Long id, HumanBeing h) {
        check(id); return realManager.update(id, h);
    }
    @Override public boolean removeById(Long id) {
        check(id); return realManager.removeById(id);
    }
    @Override public void removeAt(int index) {
        if (index >= 0 && index < realManager.getCollection().size()) {
            check(realManager.getCollection().get(index).getId());
        }
        realManager.removeAt(index);
    }
    @Override public void clear() {
        realManager.clear();
    }
    @Override public boolean isOwner(Long id, String login) {
        return realManager.isOwner(id, login);
    }
    @Override public List<HumanBeing> getCollection() {
        return realManager.getCollection();
    }
    @Override public String getInfo() {
        return realManager.getInfo();
    }
    @Override public String getShow() {
        return realManager.getShow();
    }
    @Override public String getSortedCollection() {
        return realManager.getSortedCollection();
    }
    @Override public String getSumOfImpactSpeed() {
        return realManager.getSumOfImpactSpeed();
    }
    @Override public String getDescendingMoods() {
        return realManager.getDescendingMoods();
    }
    @Override public List<HumanBeing> filterContainsName(String sub) {
        return realManager.filterContainsName(sub);
    }

    private void check(Long id) {
        if (!realManager.isOwner(id, userLogin)) {
            throw new SecurityException("Доступ запрещен: объект с ID " + id + " принадлежит другому пользователю.");
        }
    }
}