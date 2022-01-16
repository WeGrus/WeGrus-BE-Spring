package wegrus.clubwebsite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.entity.member.Role;
import wegrus.clubwebsite.repository.RoleRepository;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class SetupConfig {

    private final RoleRepository roleRepository;

    @PostConstruct
    private void setup() {
        if (roleRepository.findAll().isEmpty()) {
            for (MemberRoles role : MemberRoles.values())
                roleRepository.save(new Role(role.name()));
        }
    }
}
