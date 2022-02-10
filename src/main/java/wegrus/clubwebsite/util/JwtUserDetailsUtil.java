package wegrus.clubwebsite.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberRole;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtUserDetailsUtil implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) {
        Member findMember = memberRepository.findWithMemberRolesAndRoleById(Long.valueOf(id)).orElseThrow(MemberNotFoundException::new);

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (MemberRole role : findMember.getRoles())
            authorities.add(new SimpleGrantedAuthority(role.getRole().getName()));

        return new User(String.valueOf(findMember.getId()),
                UUID.randomUUID().toString(),
                authorities);
    }
}
