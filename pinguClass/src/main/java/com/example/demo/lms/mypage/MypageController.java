package com.example.demo.lms.mypage;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.lms.Authuser.Authuser;
import com.example.demo.lms.LoginCheck.LoginCheck;
import com.example.demo.lms.community.CommunityService;
import com.example.demo.lms.community.UserException;
import com.example.demo.lms.coupon.CouponService;
import com.example.demo.lms.course.CourseService;
import com.example.demo.lms.courseQna.QnaService;
import com.example.demo.lms.courseReview.courseReviewService;
import com.example.demo.lms.entity.Community;
import com.example.demo.lms.entity.CommunityComment;
import com.example.demo.lms.entity.Course;
import com.example.demo.lms.entity.Qna;
import com.example.demo.lms.entity.QnaAnswer;
import com.example.demo.lms.entity.Registration;
import com.example.demo.lms.entity.Review;
import com.example.demo.lms.entity.User;
import com.example.demo.lms.paging.EzenPaging;
import com.example.demo.lms.registration.RegistrationService;
import com.example.demo.lms.entity.Subscription;
import com.example.demo.lms.payment.SubscriptionService;
import com.example.demo.lms.user.UserRepository;
import com.example.demo.lms.user.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MypageController {
	// 마이페이지에서 사용자 관리를 처리하는 컨트롤러.
	
	private final CommunityService communityService;
	private final UserRepository userRepository;
	private final QnaService qnaService;
	private final CourseService courseService;
	private final UserService userService;
	private final RegistrationService registrationService;
	private final courseReviewService coursereviewService;
	private final SubscriptionService subscriptionService;
	private final CouponService couponService;

	
	// 마이페이지 사용자의 커뮤니티 작성글 조회 
	@LoginCheck
	@GetMapping("/mypage/community")
	public String getMyPageCommunityList(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			@Authuser User user) throws UserException {
		String userId = user.getId();
		EzenPaging ezenPaging = new EzenPaging(page, 10, communityService.getCommunityCountById(userId), 5);
		List<Community> communityList = this.communityService.getCommunityListByUser(userId, ezenPaging.getStartNo(),
				ezenPaging.getPageSize());
		List<CommunityComment> commentList = this.communityService.getCommentsByUser(userId, ezenPaging.getStartNo(),
				ezenPaging.getPageSize());

		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		System.out.println(user.getUserId());
		System.out.println(user.getId());
		System.out.println(communityList.size());

		model.addAttribute("communityList", communityList);
		model.addAttribute("commentList", commentList);
		model.addAttribute("page", ezenPaging);

		return "mypage/communityManage";
	}

	// 마이페이지에서 글 제목 클릭시 상세 페이지로 이동-> 커뮤니티 글 상세 조회 
	@LoginCheck
	@GetMapping("/mypage/community/{cmId}")
	public String viewCommunityDetail(@PathVariable("cmId") Integer cmId, Model model, @Authuser User user)
			throws UserException {
		Community community = communityService.getdetail(cmId);
		model.addAttribute("community", community);
		return "mypage/communityDetail";
	}
	// 글 수정 
	@LoginCheck
	@GetMapping("/mypage/community/edit/{cmId}")
	public String editCommunityForm(@PathVariable("cmId") Integer cmId, Model model) throws UserException {
		Community community = communityService.getdetail(cmId);
		model.addAttribute("community", community);
		return "mypage/communityEdit";
	}

	@LoginCheck
	@PostMapping("/mypage/community/edit/{cmId}")
	public String editCommunity(@PathVariable("cmId") Integer cmId, // 각각 pathVariable,
			@RequestParam("title") String title, // RequestParam 안 파라미터는 로그인 기능 완료시 지워줘도 됨
			@RequestParam("content") String content, @Authuser User user) throws UserException {

		// 커뮤니티 글 업데이트
		communityService.updateCommunity(cmId, title, content);

		// 수정 후 상세보기 페이지로 리다이렉트
		return "redirect:/mypage/community/" + cmId;
	}

	// 마이페이지에서 커뮤니티 작성한 글 삭제 기능 담당 해당 글의 cmId(글번호)를 받아서 삭제 기능
	@LoginCheck
	@PostMapping("/mypage/community/delete/{cmId}")
	public String deleteCommunity(@PathVariable("cmId") Integer cmId) throws UserException {
		// 커뮤니티 글 삭제
		communityService.deleteCommunity(cmId);

		// 삭제 후, 목록 페이지로 리다이렉트
		return "redirect:/mypage/community";
	}

	// 마이페이지 강좌 QnA 목록 조회
	@LoginCheck
	@GetMapping("/mypage/qna")
	public String getMyPageQnaList(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			@Authuser User user) {
		String userId = user.getId();
		EzenPaging ezenPaging = new EzenPaging(page, 10, qnaService.getQnaCountByUser(user), 5);
		List<Qna> qnaList = qnaService.getQnaListByUser(user, ezenPaging.getStartNo(), ezenPaging.getPageSize());

		System.out.println("qna 수정 아이디, userId: " + userId);

		model.addAttribute("qnaList", qnaList);
		model.addAttribute("page", ezenPaging);

		return "mypage/userMypageQnaList";
	}

	// QnA 상세 페이지 조회 , 강사 답변 조회
	@LoginCheck
	@GetMapping("/mypage/qna/{qnaId}")
	public String viewQnaDetail(@PathVariable("qnaId") Integer qnaId, Model model) {

		// QnA 상세 조회
		Qna qna = qnaService.getQnaDetail(qnaId);
		if (qna == null) {
			throw new RuntimeException("QnA not found");
		}
		model.addAttribute("qna", qna);

		// 해당 QnA의 모든 답변 조회 
		List<QnaAnswer> qnaAnswers = qnaService.getAllQnaAnswers(qnaId);

		
		System.out.println("답변 리스트: " + qnaAnswers);

		// 답변이 없는 경우에도 빈 리스트로 처리
		if (qnaAnswers == null || qnaAnswers.isEmpty()) {
			model.addAttribute("qnaAnswers", new ArrayList<>()); // 빈 리스트 전달
		} else {
			model.addAttribute("qnaAnswers", qnaAnswers);
		}

		return "mypage/userMypageQnaDetail";
	}

	// QnA 등록 페이지로 이동
	@LoginCheck
	@GetMapping("/mypage/qna/new")
	public String showQnaForm(Model model) {
		model.addAttribute("qna", new Qna());
		model.addAttribute("courses", qnaService.getAllCourses()); // 강좌 목록 추가

		// 모든 강좌 목록을 가져오는 부분 제거
		return "CourseQna/CourseQnaregistration";
	}

	// QnA 저장
	@LoginCheck
	@PostMapping("/mypage/qna/save")
	public String saveQna(@ModelAttribute Qna qna, @RequestParam(name = "courseId") Integer courseId,
			@Authuser User user) {
		qna.setUser(user);

		Course course = courseService.findById(courseId);
		if (course != null) {
			qna.setCourse(course);
		} else {
			return "redirect:/mypage/qna?error=courseNotFound";
		}

		qnaService.saveQnaWithCourse(qna, courseId);
		return "redirect:/mypage/qna";
	}

	// QnA 수정 메서드
	@LoginCheck
	@GetMapping("/mypage/qna/edit/{qnaId}")
	public String editQnaForm(@PathVariable("qnaId") Integer qnaId, Model model) {
		Qna qna = qnaService.getQnaDetail(qnaId);
		model.addAttribute("qna", qna);
		return "courseQna/CourseQnaEdit";
	}

	@LoginCheck
	@GetMapping("/mypage")
	public String mypageTest() {

		return "redirect:/mypage/edit";
	}

	// QnA 수정 처리
	@LoginCheck
	@PostMapping("/mypage/qna/edit/{qnaId}")
	public String editQna(@PathVariable("qnaId") Integer qnaId, @RequestParam("title") String title,
			@RequestParam("content") String content) {

		qnaService.updateQna(qnaId, title, content);
		return "redirect:/mypage/qna/" + qnaId;
	}

	// QnA 삭제
	@LoginCheck
	@PostMapping("/mypage/qna/delete/{qnaId}")
	public String deleteQna(@PathVariable("qnaId") Integer qnaId) {
		qnaService.deleteQna(qnaId);
		return "redirect:/mypage/qna";
	}

	// QnA 강사가 댓글을 달면 조회가 가능 .

	@LoginCheck
	@GetMapping("/mypage/courses")
	public String getEnrolledCourses(@RequestParam(value = "page", defaultValue = "0") int page, Model model,
			@Authuser User user) {
		String userId = user.getId();
		EzenPaging ezenPaging = new EzenPaging(page, 10, registrationService.countRegistrationByUser(userId), 5);
		List<Registration> registrations = registrationService.getRegistrationsByUser(userId, ezenPaging.getStartNo(),
				ezenPaging.getPageSize());

		model.addAttribute("registrations", registrations);
		model.addAttribute("page", ezenPaging);

		return "mypage/myenrolledCourses";
	}
	
	/////////////////////////////////회원정보, 구독, 쿠폰 관련//////////////////
	
	///////회원 정보 수정//////
	@LoginCheck
	@GetMapping("/mypage/edit")
	public String editUserInfo(@Authuser User u, Model model) {
		model.addAttribute("user", u);
		return "mypage/myPageUserEdit";
	}
	
	@LoginCheck
	@GetMapping("/mypage/edit/password")
	public String editUserPassword(Model model,@Authuser User u, EditPwForm editPwForm) {
		model.addAttribute("user", u);
		return "mypage/myPageEditPassword";
	}
	
	
	@LoginCheck
	@PostMapping("/mypage/edit/password")
	public String editUserPassword(@Authuser User u,@Valid EditPwForm editPwForm, BindingResult bindingResult) throws Exception {
		if(userService.prePasswordCheck(u, editPwForm.getPrePassword())) {
    		if(!editPwForm.getPassword().equals(editPwForm.getPassword2())) {
    			bindingResult.rejectValue("Password2","NoSame", "비밀번호가 일치하지 않습니다");
    			return "mypage/myPageEditPassword";
    		}else {
    			if(userService.prePasswordCheck(u, editPwForm.getPassword())) {
    				bindingResult.rejectValue("Password","SameError", "이미 사용중인 비밀번호 입니다.");
    				return "mypage/myPageEditPassword";
    			}
    		}
    		this.userService.changePassword(u, editPwForm.getPassword());
    		return "redirect:/mypage";
    	}else {
    		bindingResult.rejectValue("prePassword","NoSame", "현재 비밀번호가 일치하지 않습니다");
    		//System.out.println("비밀번호 틀려용");
    	}
		return "mypage/myPageEditPassword";
	}
	

	
	
//=====================마이페이지 구독조회===============================//
	@LoginCheck
	@GetMapping("/mypage/subscription") 
	public String subscriptionStatus(Model model, @Authuser User user){
		
		Subscription subscription = subscriptionService.getUser(user.getUserId());
		
		model.addAttribute("subscription",subscription);
		
		return "/mypage/mySubscriptionStatus";
	} 
	
	//////////////쿠폰현황///////////////////////////
	
	

    @PostMapping("/courses/cancel/{rgId}")
    public String cancelRegistration(@PathVariable("rgId") int rgId) {
    	 System.out.println("수강 취소 요청, rgId: " + rgId);
        registrationService.cancelRegistration(rgId); // 수강 신청 내역 삭제
        return "redirect:/mypage/myenrolledCourses"; // 삭제 후 목록 페이지로 리다이렉트
    }
	
	

    // 마이페이지 내에서 수강중인 강좌 리뷰 조회
   
    @LoginCheck
	@GetMapping("/mypage/CourseReview/list")
	public String listReviews(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@Authuser User user) {
		String userId = user.getId();
		EzenPaging ezenPaging = new EzenPaging(page, 10, coursereviewService.getUserReviewCount(userId), 5);
		List<Review> reviews = coursereviewService.getUserReviews(userId, ezenPaging.getStartNo(),
				ezenPaging.getPageSize());

		model.addAttribute("reviews", reviews);
		model.addAttribute("page", ezenPaging);
		return "mypage/myRegistrationreviewlist";
	}

	/* 수강 강좌 작성 리뷰 모달 내에서 수정 */
	/* 수정 버튼 클릭시 500 Internal Server Error 발생. */
    @LoginCheck
	@GetMapping("/mypage/CourseReview/get/{id}")
	@ResponseBody
	public ResponseEntity<Review> getReviewById(@PathVariable("id") Integer id) { /* id 값을 url 경로에서 받아와 리뷰데이터 반환 */
		try {
			Review review = coursereviewService.getReviewById(id); /* 해당하는 리뷰를 가져오기 */
			if (review == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			return ResponseEntity.ok(review); /* 리뷰 데이터 존재하면 리뷰 데이터 반환 */
		} catch (Exception e) {

		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

	}
    @LoginCheck
	@PostMapping("/mypage/CourseReview/edit")
	public String updateReview(@ModelAttribute Review review) {
		coursereviewService.updateReview(review);
		return "redirect:/mypage/CourseReview/list";
	}
    @LoginCheck
	@GetMapping("/mypage/CourseReview/delete/{id}")
	public String deleteReview(@PathVariable Integer id) {
		coursereviewService.deleteReview(id);
		return "redirect:/mypage/CourseReview/list";
	}
    @LoginCheck
	@PostMapping("/mypage/CourseReview/delete")
	public String deleteReviewPost(@RequestParam("reviewId") Integer id) {
		coursereviewService.deleteReview(id);
		return "redirect:/mypage/CourseReview/list";
	}

//	@LoginCheck
//	@GetMapping("/mypage/coupon")
//	public String myCoupon(@Authuser User user, Model model,
//			@RequestParam(value="page", defaultValue="0") int page){
//		
//		EzenPaging ezenPaging = new EzenPaging(page, 10, couponService.getCouponCountByKeyword(), 5);
//		List<User> userList = this.couponService.getUserByKeyword(ezenPaging.getStartNo(), ezenPaging.getPageSize());
//
//		
//		model.addAttribute("user", user);
//		model.addAttribute("page", ezenPaging);
//		model.addAttribute("kw", kw);
//		model.addAttribute("kwType", kwType);
//		
//		return "mypage/myPageCoupon";
//	}

}
	
	
	

