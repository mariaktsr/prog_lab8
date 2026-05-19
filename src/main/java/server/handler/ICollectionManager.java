package server.handler;

import common.model.HumanBeing;

import java.util.List;

public interface ICollectionManager {
    void loadFromDatabase(List<HumanBeing> data);
    void add(HumanBeing human);
    void insertAt(int index, HumanBeing human);
    boolean update(Long id, HumanBeing updated);
    boolean removeById(Long id);
    void removeAt(int index);
    void clear();
    boolean isOwner(Long id, String userLogin);
    List<HumanBeing> getCollection();

    String getInfo();
    String getShow();
    String getSortedCollection();
    String getSumOfImpactSpeed();
    String getDescendingMoods();
    List<HumanBeing> filterContainsName(String substring);
}