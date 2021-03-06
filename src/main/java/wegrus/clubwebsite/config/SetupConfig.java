package wegrus.clubwebsite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import wegrus.clubwebsite.entity.group.Groups;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.entity.post.BoardCategories;
import wegrus.clubwebsite.entity.post.Boards;
import wegrus.clubwebsite.util.AmazonS3Util;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SetupConfig {

    private final JdbcTemplate jdbcTemplate;
    private final AmazonS3Util amazonS3Util;

    @PostConstruct
    private void setup() {
        initTableRoles();
        initTableBoardCategories();
        initTableBoards();
        initTableGroups();
    }

    private void initTableBoards() {
        final List<Long> boardCategoryIdOrders = Arrays.asList(
                1L,
                2L, 2L, 2L, 2L,
                3L,
                4L, 4L, 4L, 4L, 4L, 4L
        );

        final List<Boolean> boardSecretFlags = Arrays.asList(
                false,
                false, false, false, false,
                false,
                false, false, false, false, false, true);

        final List<String> boards = Arrays.stream(Boards.values())
                .map(Boards::getKrName)
                .collect(Collectors.toList());

        final String boardSql = "INSERT INTO boards (`board_category_id`, `board_name`, `board_secret_flag`) VALUES(?, ?, ?)";
        final BatchPreparedStatementSetter boardPss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, boardCategoryIdOrders.get(i));
                ps.setString(2, boards.get(i));
                ps.setBoolean(3, boardSecretFlags.get(i));
            }

            @Override
            public int getBatchSize() {
                return boards.size();
            }
        };
        jdbcTemplate.batchUpdate(boardSql, boardPss);
    }

    private void initTableBoardCategories() {
        final List<String> boardCategories = Arrays.stream(BoardCategories.values())
                .map(BoardCategories::getKrName)
                .collect(Collectors.toList());

        final String categorySql = "INSERT INTO board_categories (`board_category_name`) VALUES(?)";
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
    }

    private void initTableRoles() {
        final List<String> roles = Arrays.stream(MemberRoles.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String sql = "INSERT INTO roles (`role_name`) VALUES(?)";
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
    }

    private void initTableGroups() {
        final List<String> groups = Arrays.stream(Groups.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String sql = "INSERT INTO igrus_groups (`group_name`) VALUES(?)";
        final BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, groups.get(i));
            }

            @Override
            public int getBatchSize() {
                return groups.size();
            }
        };
        jdbcTemplate.batchUpdate(sql, pss);
    }
}
