package wegrus.clubwebsite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.entity.post.BoardCategories;
import wegrus.clubwebsite.entity.post.BoardCategory;
import wegrus.clubwebsite.entity.post.Boards;
import wegrus.clubwebsite.repository.BoardCategoryRepository;
import wegrus.clubwebsite.repository.BoardRepository;
import wegrus.clubwebsite.repository.RoleRepository;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SetupConfig {

    private final RoleRepository roleRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardRepository boardRepository;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void setup() {
        // Roles
        roleRepository.deleteAllInBatch();

        final List<String> roles = Arrays.stream(MemberRoles.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String sql = "INSERT INTO ROLES (`role_name`) VALUES(?)";
        final BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, roles.get(i));
            }

            @Override
            public int getBatchSize() {
                return roles.size();
            }
        };
        jdbcTemplate.batchUpdate(sql, pss);

        // Board_Categories
        boardCategoryRepository.deleteAllInBatch();

        final List<String> boardCategories = Arrays.stream(BoardCategories.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String categorySql = "INSERT INTO BOARD_CATEGORIES (`board_category_name`) VALUES(?)";
        final BatchPreparedStatementSetter categoryPss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, boardCategories.get(i));
            }

            @Override
            public int getBatchSize() {
                return boardCategories.size();
            }
        };
        jdbcTemplate.batchUpdate(categorySql, categoryPss);

        // Boards
        boardRepository.deleteAllInBatch();

        List<Long> boardCategoryIds = boardCategoryRepository.findAll()
                .stream()
                .map(BoardCategory::getId)
                .collect(Collectors.toList());

        List<Long> boardCategoryIdOrders = Arrays.asList(
                boardCategoryIds.get(0),
                boardCategoryIds.get(1),
                boardCategoryIds.get(1),
                boardCategoryIds.get(1),
                boardCategoryIds.get(1),
                boardCategoryIds.get(2),
                boardCategoryIds.get(3),
                boardCategoryIds.get(3),
                boardCategoryIds.get(3),
                boardCategoryIds.get(3),
                boardCategoryIds.get(3),
                boardCategoryIds.get(3)
        );

        final List<String> boards = Arrays.stream(Boards.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String boardSql = "INSERT INTO BOARDS (`board_category_id`, `board_name`) VALUES(?, ?)";
        final BatchPreparedStatementSetter boardPss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, boardCategoryIdOrders.get(i));
                ps.setString(2, boards.get(i));

            }

            @Override
            public int getBatchSize() {
                return boards.size();
            }
        };
        jdbcTemplate.batchUpdate(boardSql, boardPss);

    }
}
