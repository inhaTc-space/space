package com.space.config.oauth;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.space.config.auth.PrincipalDetails;
import com.space.config.oauth.provider.FacebookUserInfo;
import com.space.config.oauth.provider.GoogleUserInfo;
import com.space.config.oauth.provider.KakaoUserInfo;
import com.space.config.oauth.provider.NaverUserInfo;
import com.space.config.oauth.provider.OAuth2UserInfo;
import com.space.member.constant.Role;
import com.space.member.entity.Member;
import com.space.member.repository.MemberRepository;
import com.space.mypage.category.entity.Category;
import com.space.mypage.service.SpaceService;

@Service
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private SpaceService spaceService;

	// 구글로부터 받은 userRequest 데이터에 대한 후처리되는 함수
	// 메서드 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		log.info("getClientRegistration : " + userRequest.getClientRegistration()); // registrationId로 어떤
		log.info("getAccessToken : " + userRequest.getAccessToken().getTokenValue());

		OAuth2User oAuth2User = super.loadUser(userRequest);
		// 구글로그인 버튼 클릭 -> 구글로그인 창 -> 로그인을 완료 -> code를 리턴(OAuth-Client라이브러리) -> AccessToken요청
		// userRequest정보 -> loadUser함수 호출 -> 구글로부터 회원프로필 받아준다.
		log.info("getAttributes : " + oAuth2User.getAttributes());
		
		// 회원가입 강제로 함
		OAuth2UserInfo oauth2UserInfo = null;
		if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
			log.info("구글 로그인 요청입니다.");
			oauth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
		} else if(userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
			log.info("카카오 로그인 요청입니다.");
			oauth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
		} else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
			log.info("페이스북 로그인 요청입니다.");
			oauth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
		} 
		else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
			log.info("네이버 로그인 요청입니다.");
			oauth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
		}
		else  {
			log.info("구글,카카오,네이버,페이스북만 지원합니다!");
		}
		
		String provider = oauth2UserInfo.getProvider(); // google
		String providerId = oauth2UserInfo.getProviderId();
		String email = oauth2UserInfo.getEmail();
		String password = passwordEncoder.encode("키위");
		String name = oauth2UserInfo.getName();
		String image = oauth2UserInfo.getImage();
		
		Member memberEntity = memberRepository.findByEmail(email);
		
		// DB에 회원이 없을때 강제가입
		if(memberEntity == null) {
			log.info("OAuth2 로그인이 최초입니다.");
			log.info("자동회원가입이 진행됩니다.");
			memberEntity = Member.builder()
					.email(email)
					.password(password)
					.provider(provider)
					.providerId(providerId)
					.role(Role.ADMIN)
					.name(name)
					.image(image)
					.allPublicYn("Y")
					.build();
			memberRepository.save(memberEntity);	
			spaceService.createCategory(Category.createCategory(), memberEntity);		// 회원가입시 카테고리 생성
		} else {
			log.info("OAuth2 로그인한 적이 있습니다. 자동회원가입이 되어있습니다.");
		}
		
		
		return new PrincipalDetails(memberEntity,oAuth2User.getAttributes());
	}
}
