package com.example.demo.lms.admin;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.lms.LoginCheck.LoginCheck;
import com.example.demo.lms.entity.Category;
import com.example.demo.lms.entity.Community;
import com.example.demo.lms.entity.Course;
import com.example.demo.lms.entity.CsAnswer;
import com.example.demo.lms.entity.CsQuestion;
import com.example.demo.lms.entity.Lecture;
import com.example.demo.lms.entity.Notice;
import com.example.demo.lms.entity.Report;
import com.example.demo.lms.entity.User;
import com.example.demo.lms.file.FileService;
import com.example.demo.lms.paging.EzenPaging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class AdminController {
	
	private final AdminService adService;
	private final FileService fileService;

	/*************************************** 관리자 로그인 ***************************************/
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 관리자 GET ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin")
	public String adminLogin(AdminForm adminform) {
		
		return "admin/adminLogin";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 관리자 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@PostMapping("/admin/login")
	public String adminSelect(@Valid AdminForm adminform, BindingResult bindingResult) {
		
		if(bindingResult.hasErrors()) {
			//@Valid 유효성 검사 실패시
			
			return "redirect:/admin";
		}
		
		return "admin/userList";
	}
	
	/*************************************** 회원 관리 ***************************************/
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/userList")
	public String userList(Model model, @RequestParam(value="page", defaultValue="0") int page, 
							@RequestParam(value = "kw", defaultValue = "") String kw,
							@RequestParam(value = "kwType", defaultValue = "") String kwType,
							HttpSession session) {
		
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getUserCountByKeyword(kwType, kw), 5);
		List<User> userList = this.adService.getUserByKeyword(kwType, kw, ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("userList", userList);
		model.addAttribute("page", ezenPaging);
		model.addAttribute("kw", kw);
		model.addAttribute("kwType", kwType);

		return "admin/adminUserList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강사 등록 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@PostMapping("/admin/setInst")
	public String createInstructor(@RequestParam(value="userId") Integer userId, HttpServletRequest request,
								@RequestParam(value = "instImg") MultipartFile instImgFile) throws Exception {
		
		Integer fileId = this.fileService.save(request, instImgFile); //파일 복사 + file 테이블 insert, file_id 반환
		
		this.adService.createInstructor(userId, fileId);
		
		return "redirect:/admin/userList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 정지 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/banned/{id}")
	public String bannedUser(@PathVariable("id") Integer userId) {
		
		this.adService.bannedUser(userId, "y");
		
		return "redirect:/admin/userList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 정지 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/banncancel/{id}")
	public String bannCancelUser(@PathVariable("id") Integer userId) {
		
		this.adService.bannedUser(userId, "n");
		
		return "redirect:/admin/userList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/signout/{id}")
	public String signoutUser(@PathVariable("id") Integer userId) {
		
		this.adService.signoutUser(userId, "y");
		
		return "redirect:/admin/userList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 삭제 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/signoutcancel/{id}")
	public String signoutCancelUser(@PathVariable("id") Integer userId) {
		
		this.adService.signoutUser(userId, "n");
		
		return "redirect:/admin/userList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 신고 내역 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/reportList/{id}/{page}")
	public String reportUserList(@PathVariable("id") Integer userId, Model model,
								@PathVariable("page") int page) {
		
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getUserCountByReportId(userId), 5);
		List<Report> reportList = this.adService.getUserByReportId(userId, ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("reportList", reportList);
		model.addAttribute("page", ezenPaging);
		
		return "/admin/adminUserListReport";
	}
	
	
	/*************************************** 강좌 관리 ***************************************/
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강좌 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/courseList")
	public String courseList(Model model, @RequestParam(value="page", defaultValue="0") int page, 
							@RequestParam(value = "kw", defaultValue = "") String kw,
							@RequestParam(value = "kwType", defaultValue = "") String kwType) {
		
		EzenPaging ezenPaging = new EzenPaging(page, 3, adService.getCourseCountByKeyword(kwType, kw), 5);
		List<Course> courseList = this.adService.getCourseByKeyword(kwType, kw, ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("courseList", courseList);
		model.addAttribute("page", ezenPaging);
		model.addAttribute("kw", kw);
		model.addAttribute("kwType", kwType);
		
		return "admin/adminCourseList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강좌 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deletecourse/{id}")
	public String deleteCourse(@PathVariable("id") Integer courseId) {
		
		this.adService.deleteCourse(courseId, "y");
		
		return "redirect:/admin/courseList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강좌 삭제 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deletecourse/cancel/{id}")
	public String deleteCancelCourse(@PathVariable("id") Integer courseId) {
		
		this.adService.deleteCourse(courseId, "n");
		
		return "redirect:/admin/courseList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 특정 강좌의 강의 내역 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/lectureList/{id}/{page}")
	public String lectureList(@PathVariable("id") Integer courseId, Model model,
								@PathVariable("page") int page) {
		
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getLectureCountByCourseId(courseId), 5);
		List<Lecture> lectureList = this.adService.getLectureByCourseId(courseId, ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("lectureList", lectureList);
		model.addAttribute("page", ezenPaging);
		
		return "/admin/adminCourseLectureList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강의 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deletelecture/{lectureId}/{courseId}/{page}")
	public String deleteLecture(@PathVariable("lectureId") Integer lectureId,
								@PathVariable("courseId") Integer courseId,
								@PathVariable("page") Integer page) {
		
		this.adService.deleteLecture(lectureId, "y");
		
		return "redirect:/admin/lectureList/" + courseId + "/" + page;
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 강의 삭제 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deletelecture/cancel/{lectureId}/{courseId}/{page}")
	public String deleteCancelLecture(@PathVariable("lectureId") Integer lectureId,
										@PathVariable("courseId") Integer courseId,
										@PathVariable("page") Integer page) {
		
		this.adService.deleteLecture(lectureId, "n");
		
		return "redirect:/admin/lectureList/" + courseId + "/" + page;
	}
	/*************************************** 커뮤니티 관리 ***************************************/	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ전체 커뮤니티 글 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/communityList")
	public String communityList(Model model, @RequestParam(value="page", defaultValue="0") int page, 
								@RequestParam(value = "kw", defaultValue = "") String kw,
								@RequestParam(value = "kwType", defaultValue = "") String kwType) {
		
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getCommunityCountByKeyword(kwType, kw), 5);
		List<Community> communityList = this.adService.getCommunityByKeyword(kwType,kw, ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("communityList", communityList);
		model.addAttribute("page", ezenPaging);
		model.addAttribute("kw", kw);
		model.addAttribute("kwType", kwType);
		
		return "/admin/adminCommunityList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 커뮤니티 글의 작성글 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/communityContent/{id}")
	public String communityContent(@PathVariable("id") Integer cmId,Model model){ 
		
		Community community = this.adService.getCommunity(cmId);
		
		model.addAttribute("community",community);
		return "/admin/adminCommunityContent";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 커뮤니티 글 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deleteCommunity/{id}")
	public String signoutContent(@PathVariable("id") Integer cmId) {
		
		this.adService.signoutContent(cmId, "y");
		
		return "redirect:/admin/communityList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 커뮤니티 글 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/cancelcommunity/{id}")
	public String signoutCancel(@PathVariable("id") Integer cmId) {
		
		this.adService.signoutContent(cmId, "n");
		
		return "redirect:/admin/communityList";
	}
	
	/*************************************** 공지사항 관리 ***************************************/
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 전체 공지사항 글 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/adminNoticeList")
	public String adminAnnounceList(Model model, @RequestParam(value="page", defaultValue="0") int page) {
		
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getNoticeCountByKeyword(), 5);
		List<Notice> noticeList = this.adService.getNoticeByKeyword(ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("page", ezenPaging);

		
		return "/admin/adminNoticeList";
	}	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항 작성글 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/adminNoticeListDetail/{id}")
	public String adiminNoticeDetail(@PathVariable("id") Integer noticeId,Model model){ 
		
		Notice notice = this.adService.getNotice(noticeId);
		
		model.addAttribute("notice",notice);
		return "/admin/adminNoticeListDetail";	
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항 작성글 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deleteNotice/{id}")
	public String signoutNoticeDetail(@PathVariable("id") Integer noticeId) {
		
		this.adService.signoutDetail(noticeId, "y");
		
		return "redirect:/admin/adminNoticeList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항글 글 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/cancelNotice/{id}")
	public String signoutNoticeDetailCancel(@PathVariable("id") Integer noticeId) {
		
		this.adService.signoutDetail(noticeId, "n");
		
		return "redirect:/admin/adminNoticeList";
	}
	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항 작성 페이지 이동 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@GetMapping("/admin/adminNoticeList/registerPage")
	public String moveAnnouncementRegistration(NoticeForm noticeForm) {
		
		return "/admin/adminRegistNotice";
	}	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항 등록 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@PostMapping("/admin/adminNoticeList/registerPage")
	public String registAnnouncement(@Valid NoticeForm noticeForm,
			BindingResult bindingResult) {
		
		if(bindingResult.hasErrors()) {
			return "/admin/adminRegistNotice";
		}
		
 
		String adminCode = "qwer1234"; //principal.getName()
		
		
		this.adService.register(noticeForm.getTitle(), noticeForm.getContents(), adminCode);
		
		return "redirect:/admin/adminNoticeList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 공지사항 수정 페이지 이동 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/adminNoticeList/{id}")
	public String questionModify(NoticeForm noticeForm,@PathVariable("id") Integer noticeId, Model model){
		
		Notice n = this.adService.getNotice(noticeId);
		
//		model.addAttribute(null, n);
		noticeForm.setTitle(n.getTitle());
		noticeForm.setContents(n.getContent());
		
		return "/admin/adminRegistNotice";
	}
	
	@PostMapping("/admin/adminNoticeList/{id}")
	public String questionModify(@Valid NoticeForm noticeForm, 
			@PathVariable("id") Integer noticeId, BindingResult bindingResult){
		
		if(bindingResult.hasErrors()) {
			return "/admin/adminRegistNotice";
		}
		
		Notice n = this.adService.getNotice(noticeId);
		this.adService.modify(n, noticeForm.getTitle(), noticeForm.getContents());
		
		return "redirect:/admin/adminNoticeList";
	}	
	

	/*************************************** 1대1 문의 관리 ***************************************/
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 전체  문의글 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/adminOneToOneList")
	public String adminOneToOneList(Model model, @RequestParam(value="page", defaultValue="0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "kwType", defaultValue = "") String kwType) {
									
		//EzenPaging ezenPaging = new EzenPaging(현재 페이지 번호, 페이지당 글 갯수, 총 글 갯수, 페이징 버튼 갯수)
		EzenPaging ezenPaging = new EzenPaging(page, 10, adService.getCsQuestionCountByKeyword(kwType, kw), 5);
		List<CsQuestion> questionList = this.adService.getCsQuetionByKeyword(kwType,kw,ezenPaging.getStartNo(), ezenPaging.getPageSize());
		
		model.addAttribute("questionList", questionList);
		model.addAttribute("page", ezenPaging);
		model.addAttribute("kw", kw);
		model.addAttribute("kwType", kwType);
		
		return "/admin/adminOneToOne";
	}	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 1대1 문의 / 답변 조회 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/adminOneToOneListDetail/{id}")
	public String adiminOneToOneDetail(@PathVariable("id") Integer questionId,Model model){ 
		
		CsQuestion question = this.adService.getQuestion(questionId);
		
		model.addAttribute("question",question);
		return "/admin/adminOneToOneAnswerDetail";	
	}
	
	
	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 1대1 문의 수정 페이지 이동 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@GetMapping("/admin/adminOneToOneList/modifyPage/{id}")
	public String moveAnswerModify(AnswerForm answerForm, @PathVariable("id") Integer answerId) {
		
		CsAnswer a = this.adService.getAnswer(answerId);
		answerForm.setTitle(a.getTitle());
		answerForm.setContents(a.getContent());
		
		return "/admin/adminRegistAnswer";
	}	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 답변 수정  ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@PostMapping("/admin/adminOneToOneList/modifyPage/{id}")
	public String ModifyAnswer(@PathVariable("id") Integer id,@Valid AnswerForm answerForm,
			BindingResult bindingResult) {
		
		if(bindingResult.hasErrors()) {
			return "/admin/adminRegistAnswer";
		}
		
		String adminCode = "qwer1234"; //principal.getName()
		
		this.adService.modifyAnswer(id, answerForm.getTitle(), answerForm.getContents(), adminCode);
		
		return "redirect:/admin/adminOneToOneList";
	}	

	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 1대1 문의 등록 페이지 이동 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@GetMapping("/admin/adminOneToOneList/registerPage/{id}")
	public String moveAnswerRegistration(AnswerForm answerForm, @PathVariable("id") Integer questionId, Model model) {
		
		model.addAttribute("questionId", questionId);
		return "/admin/adminRegistAnswer";
	}	

	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 답변 등록 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	
	@PostMapping("/admin/adminOneToOneList/registerPage/{id}")
	public String registAnswer(@PathVariable("id") Integer id,@Valid AnswerForm answerForm,
			BindingResult bindingResult) {
		
		if(bindingResult.hasErrors()) {
			return "/admin/adminRegistAnswer";
		}
		
		String adminCode = "qwer1234"; //principal.getName()
		
		
		this.adService.registAnswer(answerForm.getQuestionId(), answerForm.getTitle(), answerForm.getContents(), adminCode);
		
		return "redirect:/admin/adminOneToOneList";
	}	
	
	
	
	
	
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 1대1 문의 답변 삭제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/deleteAnswer/{id}")
	public String signoutAnswer(@PathVariable("id") Integer answerId) {
		
		this.adService.signoutAnswer(answerId, "y");
		
		return "redirect:/admin/adminOneToOneList";
	}
	
	/* ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 1대1 문의 답변 삭제 해제 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
	@GetMapping("/admin/seleteAnswerCancel/{id}")
	public String signoutAnswerCancel(@PathVariable("id") Integer answerId) {
		
		this.adService.signoutAnswer(answerId, "n");
	
		return "redirect:/admin/adminOneToOneList";
	}	
	

	
	
	
	
} //class END










