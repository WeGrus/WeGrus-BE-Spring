package wegrus.clubwebsite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.entity.post.BoardCategories;
import wegrus.clubwebsite.repository.BoardCategoryRepository;
import wegrus.clubwebsite.repository.RoleRepository;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SetupConfig {

    private final RoleRepository roleRepository;
    private final BoardCategoryRepository boardCategoryRepository;
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
    }
}
