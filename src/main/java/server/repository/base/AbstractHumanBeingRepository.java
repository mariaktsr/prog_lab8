package server.repository.base;

import common.model.HumanBeing;
import server.repository.HumanBeingRepository;

import java.sql.SQLException;
import java.util.List;

//Абстрактный базовый класс для реализации репозитория HumanBeing.
public abstract class AbstractHumanBeingRepository implements HumanBeingRepository {

    //Валидация объекта HumanBeing перед сохранением или обновлением
    protected void validateHumanBeing(HumanBeing human) throws RepositoryException {
        if (human == null) {
            throw new RepositoryException("HumanBeing не может быть null");
        }
        if (human.getName() == null || human.getName().trim().isEmpty()) {
            throw new RepositoryException("Имя не может быть пустым или состоять из пробелов");
        }
        if (human.getCoordinates() == null) {
            throw new RepositoryException("Координаты не могут быть null");
        }
        if (human.getCoordinates().getY() == null || human.getCoordinates().getY() <= -228) {
            throw new RepositoryException("Координата Y должна быть больше -228");
        }
        if (human.getRealHero() == null) {
            throw new RepositoryException("Поле realHero не может быть null");
        }
        if (human.getImpactSpeed() == null || human.getImpactSpeed() <= -428) {
            throw new RepositoryException("impactSpeed не может быть null и должен быть больше -428");
        }
        if (human.getWeaponType() == null) {
            throw new RepositoryException("weaponType не может быть null");
        }
        if (human.getMood() == null) {
            throw new RepositoryException("mood не может быть null");
        }
        if (human.getOwner() == null || human.getOwner().getLogin() == null || human.getOwner().getLogin().trim().isEmpty()) {
            throw new RepositoryException("Владелец объекта и его логин не могут быть null или пустыми");
        }
    }

    //Абстрактные методы интерфейса (реализуются в конкретных классах)
    @Override public abstract void initSchema() throws SQLException;
    @Override public abstract List<HumanBeing> findAll() throws SQLException;
    @Override public abstract boolean save(HumanBeing human) throws SQLException;
    @Override public abstract boolean update(HumanBeing human) throws SQLException;
    @Override public abstract boolean deleteById(Long id) throws SQLException;
    @Override public abstract long getNextId() throws SQLException;
    @Override public abstract void saveAll(List<HumanBeing> collection) throws SQLException;
}