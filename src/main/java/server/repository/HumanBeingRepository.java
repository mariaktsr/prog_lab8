package server.repository;

import common.model.HumanBeing;

import java.sql.SQLException;
import java.util.List;

public interface HumanBeingRepository {

    void initSchema() throws SQLException;
    List<HumanBeing> findAll() throws SQLException;
    boolean save(HumanBeing human) throws SQLException;
    boolean update(HumanBeing human) throws SQLException;
    boolean deleteById(Long id) throws SQLException;
    long getNextId() throws SQLException;
    void saveAll(List<HumanBeing> collection) throws SQLException;
}