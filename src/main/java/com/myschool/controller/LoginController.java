package com.myschool.controller;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.myschool.service.KakaoAPI;
import com.myschool.util.Util;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.myschool.VO.AttendStudentVO;
import com.myschool.VO.CommunityVO;
import com.myschool.VO.ItemVO;
import com.myschool.VO.KakaoVO;
import com.myschool.VO.MemoVO;
import com.myschool.VO.RentalVO;
import com.myschool.VO.StudentInfoVO;
import com.myschool.VO.StudentVO;
import com.myschool.VO.doneVO;
import com.myschool.service.StudentService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;



@Controller
public class LoginController {
	
    @Autowired
    private StudentService service;
    @Autowired
    private KakaoAPI kakao;
    @Autowired
    ServletContext application;
 
    
    // QR?????? ?????? ?????? ?????????
	@GetMapping("/video")
	public String loginView(@RequestParam("value") String value, Model model) {
		if(value.equals("yes")) {
			model.addAttribute("value","??????");
		}
		else {
			model.addAttribute("value","??????");
		}
		return "index"; 	
	}

	// QR?????? ????????????
	@RequestMapping(value="/check", method = RequestMethod.POST,produces = "application/text; charset=utf8")
	@ResponseBody
	public String check(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam("num") String num,
			@RequestParam("attend") String attend) throws Exception{

		SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
    	Calendar calendar = Calendar.getInstance();
    	StudentVO vo = new StudentVO();
    	Date dateObj = calendar.getTime();
    	String formattedDate = dtf.format(dateObj);
    	
    	System.out.println(attend);
    	Date dd = dtf.parse(formattedDate);
		if(attend.equals("??????")) {
			System.out.println("ok");
        	vo.setNum(num);
        	vo.setName(name);
        	vo.setAttend(attend);
        	vo.setDate(formattedDate);
        	vo.setTime("");
		} else {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd");
			Date dat = calendar.getTime();
			String d_date = dt.format(dat);

			String a = service.getCheckDate(d_date, num);
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
			Date to = transFormat.parse(a);
			
			long tt = (dd.getTime() - to.getTime()) / 1000 / 60;
			
        	vo.setNum(num);
        	vo.setName(name);
        	vo.setAttend(attend);
        	vo.setDate(formattedDate);
        	vo.setTime(String.valueOf(tt));
			System.out.println(tt);
		}
        
  
        
        if(vo.getName().equals("")) {
        	return "false";
        }
		service.attendStudent(vo);
		
		String send = "{\"on\":\"success\"," + "\"name\":\"" + vo.getName() + "\","+ "\"attend\":\""+ vo.getAttend() + "\"," + "\"date\":\"" + vo.getDate()+"\"}";
		return send;
	}
	
	
	// ??????, ?????? ????????? ?????????
	@GetMapping("/choose")
	public String chooseView(Model model) {
		 Date from = new Date();
         SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
         String dated = date.format(from);
        List<StudentVO> attending = service.getAttendingList(dated);
        List<StudentVO> attended = service.getAttendedList(dated);
        
        attending.removeAll(attended);
        
        model.addAttribute("attended", attending);
		return "choose"; 	
	}
	
	// ?????? ????????? ????????? ?????????
	@GetMapping("/studentlogin")
	public String studentLoginView() {
		return "studentlogin"; 	
	}
	
	@GetMapping("/")
	public String mainloginView() {
		return "studentlogin";
	}
	
	
	
	// ?????? ?????? ????????? ?????????
	@GetMapping("/studentregister")
	public String studentregisterview() {
		return "studentregister"; 	
	}
	
	// ?????? ?????? ????????? ?????????
	@GetMapping("/main")
	public String mainview(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        

        model.addAttribute("t", full);
		model.addAttribute("attend",attend);
		
		 Date from = new Date();
         SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
         String dated = date.format(from);
         List<StudentVO> attending = service.getAttendingList(dated);
         List<StudentVO> attended = service.getAttendedList(dated);
         
         attending.removeAll(attended);
         
         
         List<AttendStudentVO> StudentList = new ArrayList<AttendStudentVO>();
         for(StudentVO student: attending) {
        	AttendStudentVO s = new AttendStudentVO();
        	String img = service.getStudentimgsrc(student.getNum());
        	s.setNum(student.getNum());
        	s.setName(student.getName());
        	s.setImgsrc(img);
        	s.setTime(student.getTime());
        	s.setAttend(student.getAttend());
        	s.setDate(student.getDate());
        	StudentList.add(s);
         }
         
         List<MemoVO> memolist = new ArrayList<MemoVO>();
         memolist = service.getMemoList();
         
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("memo",memolist);
         model.addAttribute("user",userSet);
         model.addAttribute("attending", StudentList);
         
         List<CommunityVO> communityList = new ArrayList<CommunityVO>();
         communityList = service.getCommunityList();
         model.addAttribute("communityList",communityList);
         
		return "main"; 	
	}
	
	
	@GetMapping("/adminmain")
	public String adminmainview(Model model) {
		 Date from = new Date();
         SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
         String dated = date.format(from);
         List<StudentVO> attending = service.getAttendingList(dated);
         List<StudentVO> attended = service.getAttendedList(dated);
         attending.removeAll(attended);
         model.addAttribute("attending", attending);
         
         
         List<StudentInfoVO> allstudent = service.getAllUser();
         model.addAttribute("allstudent", allstudent);
         
		return "adminmain"; 	
	}
	
	@PostMapping("/adminmain")
	public String adminmainviewPost(StudentInfoVO vo, Model model)  {
		List<StudentVO> attend = service.getAttendList(vo.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, vo.getName());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        

        model.addAttribute("t", full);
		model.addAttribute("attend",attend);
		
		
		 Date from = new Date();
         SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
         String dated = date.format(from);
         List<StudentVO> attending = service.getAttendingList(dated);
         List<StudentVO> attended = service.getAttendedList(dated);
         attending.removeAll(attended);
         model.addAttribute("attending", attending);
         

         
         List<StudentInfoVO> allstudent = service.getAllUser();
         model.addAttribute("allstudent", allstudent);
         
         vo.getName();
         
		return "adminmain"; 	
	}
	
	
	@PostMapping("/adminattendstu")
	public String adminattend(StudentVO vo) throws ParseException {
    	Calendar calendar = Calendar.getInstance();
    	vo.setName(service.getStudentName(vo.getNum()));
    	String uuid ="";
    	String name = vo.getName();
		
		
    	if(vo.getAttend().equals("??????")) {
    		vo.setTime("");
    	}
    	else {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd");
			Date dat = calendar.getTime();
			String d_date = dt.format(dat);
			
			SimpleDateFormat transFormat1 = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
			Date to1 = transFormat1.parse(vo.getDate());

			String a = service.getCheckDate(d_date, vo.getNum());
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
			Date to = transFormat.parse(a);
			
			long tt = (to1.getTime() - to.getTime()) / 1000 / 60;
        	vo.setTime(String.valueOf(tt));
    	}
		
    	service.attendStudent(vo);
		
		return "redirect:/adminmain";
	}
	
	
	@GetMapping("/qrcode")
	public String qrView() {
		return "qrcode"; 	
	}
	
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "studentlogin"; 	
	}
	
	/* QR?????? ???????????? ????????? ????????? ?????????
	@GetMapping("/login")
	public String loginView() {
		return "login"; 	
	}
	*/
	
	/* KaKao ????????? API
	@RequestMapping(value="/login/kakao")
	public String Kakaologin(@RequestParam("code") String code, HttpSession session,HttpServletRequest request, Model model) {
		KakaoVO kakaoVO = new KakaoVO();
	    kakaoVO = kakao.getAccessToken(code);
	    HashMap<String, String> userInfo = kakao.getUserInfo(kakaoVO.getAccess_token());

	    application.setAttribute("access_Token", kakaoVO.getAccess_token());
	    application.setAttribute("refresh_Token", kakaoVO.getRefresh_token());
	    
	    return "choose";
	}
	
	// ????????? ?????? ????????????
	@RequestMapping(value="/refresh", method = RequestMethod.POST,produces = "application/text; charset=utf8")
	@ResponseBody
	public String refresh(HttpSession session) {
		String refresh_token = (String)application.getAttribute("refresh_Token");
		System.out.print(refresh_token);
		KakaoVO token = kakao.refreshToken(refresh_token);
		application.setAttribute("access_Token", token.getAccess_token());
		System.out.println("access: " + token.getAccess_token());
		
		return "sucess";
	}
	
	*/
	
	// ?????? ?????????
	@RequestMapping(value="/stulogin", produces="application/text; charset=utf8")
	@ResponseBody
	public String login(@RequestParam("userNum") String userNum,
			@RequestParam("userPwd") String userPwd, HttpSession session) throws Exception {
		
		StudentInfoVO vo = new StudentInfoVO();
		

		// ????????? ??????????????? ID??? Salt?????? ???????????? ???????????? ??????
		String salt = service.getStudentSalt(userNum);
		
		
		// salt ?????? DB??? ?????????
		if (salt != null) {
			// ????????? salt ????????? ?????????
			String PW = Util.Hashing(userPwd.getBytes(), salt);
			// ???????????? ?????? vo??? ??????
			vo.setNum(userNum);
			vo.setPwd(PW);
		} else {
			System.out.println("[??????] ????????? ??????");
			return "????????? ID ?????? ??????????????? ?????????????????????";
		}
		
		
		// ???????????? ?????? ????????? ??????
		
		StudentInfoVO user = service.getStudentInfo(vo);
		
		// DB??? ?????? ????????? ???????????????
		if (user != null) {
			// ???????????? ????????? ???????????????
			session.setAttribute("user", user);
			System.out.println("[??????]" + user.getName() + "?????? ????????????????????????");
			
			if(user.getName().equals("?????????")) {
				return "??????????????????";
			} else {
			return "?????????";
			}
		} else {
			System.out.println("[??????] ???????????? ??????");
			return "????????? ID ?????? ??????????????? ?????????????????????"; 
		}
	}
	
	@PostMapping("/sturegister")
	public String register(StudentInfoVO vo,HttpSession session,
			HttpServletRequest request) throws Exception {
		// Salt??? ????????????
		String SALT = Util.getSALT();
		// ???????????? ???????????????

		String PWD = Util.Hashing(vo.getPwd().getBytes(), SALT);

		
		// salt????????? ???????????? ???????????? ????????????
		vo.setSalt(SALT);
		vo.setPwd(PWD);
		
		System.out.println(vo.getNum());
		System.out.println(vo.getName());
		System.out.println(vo.getPwd());
		System.out.println(vo.getSalt());

		// ??????????????? ???????????? ?????????
		service.insertStudent(vo);
		
		// ????????? ????????? ?????? ????????? ??????
		// String webappRoot = request.getSession().getServletContext().getRealPath("/");
		String webappRoot ="";
	
		// ??????????????? ???????????? ?????????(File.separator??? ??????????????? ?????? ????????? ??????)
		String relativeFolder = "resources" + File.separator + "user" + File.separator;
			
		try {
            File file = null;
            // ?????????????????? ????????? ???????????? ??????
            file = new File(webappRoot + relativeFolder);
            if(!file.exists()) {
                file.mkdirs();
            }
            // ??????????????? ????????? URL??????
            
            String code = "{\"name\": \""+ vo.getName() + "\",\"num\": \""+vo.getNum()+ "\"}";
            System.out.println(code);
            String codeurl = new String(code.getBytes("UTF-8"), "ISO-8859-1");
            // ???????????? ????????? ?????????
            int qrcodeColor =   0xFF2e4e96;
            // ???????????? ???????????????
            int backgroundColor = 0xFFFFFFFF;
             
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // 3,4?????? parameter??? : width/height??? ??????
            BitMatrix bitMatrix = qrCodeWriter.encode(codeurl, BarcodeFormat.QR_CODE,800, 800);
            //
            MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(qrcodeColor,backgroundColor);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix,matrixToImageConfig);
            // ImageIO??? ????????? ????????? ????????????
            ImageIO.write(bufferedImage, "png", new File(webappRoot + relativeFolder + vo.getNum() + "_qrcode.png"));
             
        } catch (Exception e) {
            e.printStackTrace();
        }  

		
		return "studentlogin";
	}
	
	@RequestMapping(value="/profileimg", method=RequestMethod.POST) //post ??????
	public String profileimg(@RequestParam("imgfile") MultipartFile file, @RequestParam("num") String num,HttpServletRequest request,HttpSession session) throws IllegalStateException, IOException {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
	// ????????? ????????? ?????? ????????? ?????????
	
	//String webappRoot = request.getSession().getServletContext().getRealPath("/");
	// String webappRoot = "C:/Users/korea/Desktop/SchoolAttend/src/main/webapp/";
		String webappRoot ="C:/Users/PC/Desktop/SchoolAttend/src/main/webapp/";
	// ??????????????? ???????????? ?????????(File.separator??? ??????????????? ?????? ????????? ??????)
	String relativeFolder = "resources" + File.separator + "user" + File.separator;
	
	
	StudentInfoVO vo = new StudentInfoVO();
	
	UUID uuid = UUID.randomUUID();
	
		// ????????? ?????? ????????????
		if (!file.isEmpty()) {
			// ????????? ????????? ????????????
			String originalFileName = file.getOriginalFilename();
			// ?????? ????????? ??????????????? ????????? ????????????.
			String newFileName = uuid.toString() + "_" + originalFileName ; 
			file.transferTo(new File(webappRoot + relativeFolder + newFileName));
			// ????????? ?????? ?????? VO??? ?????? ????????? ?????????.
			vo.setImgsrc(newFileName);
		}
		// DB??? ?????? ????????? ???????????? ?????????.
		vo.setNum(user.getNum());
		System.out.println(vo.getImgsrc());
		
		service.updateImg(vo);
		return "redirect:/main";
	}
	
	@GetMapping("/addmemo")
	@ResponseBody 
	public String addmemo(@RequestParam("memo") String memo, HttpSession session) throws Exception{
		
		Date date_now = new Date(System.currentTimeMillis()); // ??????????????? ????????? Date????????? ????????????
		// ?????????????????? 14?????? ??????
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
		String date = fourteen_format.format(date_now);
		
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		
		String userNum = user.getNum();
		
		MemoVO memo_text = new MemoVO();
		memo_text.setDate(date);
		memo_text.setNum(userNum);
		memo_text.setName(user.getName());
		memo_text.setMemo(memo);
		service.insertMemo(memo_text);
		
		return "success";
	}
	
	// ?????? ????????? ????????? ?????????
	@GetMapping("/community")
	public String communityView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<CommunityVO> communityList = new ArrayList<CommunityVO>();
         communityList = service.getCommunityList();
         model.addAttribute("communityList",communityList);
         
         List<CommunityVO> communityListNotice = new ArrayList<CommunityVO>();
         
         
		return "community"; 	
	}
	
	@GetMapping("/community_write")
	public String communityWriteView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
		return "community_write"; 	
	}
	
	@GetMapping("/studentlist")
	public String studentlistView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<StudentInfoVO> stulist = new ArrayList<StudentInfoVO>();
         stulist = service.getStuList();
         
         model.addAttribute("studentlist", stulist);
         
		return "studentlist"; 	
	}
	
	
	
	@PostMapping("/communitywrite")
	public String communityWrite(@RequestParam("filesrc") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("categori") String categori,
			@RequestParam("content") String content,
			HttpSession session,
			HttpServletRequest request) throws Exception {
		
		CommunityVO vo = new CommunityVO();
		vo.setTitle(title);
		vo.setCategori(categori);
		vo.setContent(content);

		StudentInfoVO student = (StudentInfoVO)session.getAttribute("user");
		vo.setName(student.getName());
		vo.setNum(student.getNum());
		
		// ????????? ????????? ?????? ????????? ?????????
		
		//String webappRoot = request.getSession().getServletContext().getRealPath("/");
		String webappRoot ="";
		// ??????????????? ???????????? ?????????(File.separator??? ??????????????? ?????? ????????? ??????)
		String relativeFolder = "resources" + File.separator + "upload" + File.separator;
		
		Date date_now = new Date(System.currentTimeMillis()); // ??????????????? ????????? Date????????? ????????????
		// ?????????????????? 14?????? ??????
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
		String date = fourteen_format.format(date_now);
		
		UUID uuid = UUID.randomUUID();
		
			// ????????? ?????? ????????????
			if (!file.isEmpty()) {
				// ????????? ????????? ????????????
				String originalFileName = file.getOriginalFilename();
				// ?????? ????????? ??????????????? ????????? ????????????.
				String newFileName = uuid.toString() + "_" + originalFileName ; 
				file.transferTo(new File(webappRoot + relativeFolder + newFileName));
				// ????????? ?????? ?????? VO??? ?????? ????????? ?????????.
				vo.setFilesrc(newFileName);
			}
			vo.setDate(date);
			
			service.insertCommunity(vo);
		return "redirect:/community";
	}
	
	@GetMapping("/community_info")
	public String communityinfoView(@RequestParam("numb") String numb, Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<StudentInfoVO> stulist = new ArrayList<StudentInfoVO>();
         stulist = service.getStuList();
         
         CommunityVO com = service.getCommunityInfo(numb);
         
         
         model.addAttribute("studentlist", stulist);
         model.addAttribute("com",com);
         
		return "community_info"; 	
	}
	
	@PostMapping("/communitydelete")
	public String communitydeleteView(@RequestParam("numb") String numb) {
		
		// ????????? ????????? ?????? ????????? ??????
		// String webappRoot = request.getSession().getServletContext().getRealPath("/");
		String webappRoot ="";
		// ??????????????? ???????????? ?????????(File.separator??? ??????????????? ?????? ????????? ??????)
		String relativeFolder = "resources" + File.separator + "upload" + File.separator;
		
		CommunityVO vo = service.getCommunityInfo(numb);
		
		File file = new File(webappRoot + relativeFolder + vo.getFilesrc()); 
		if( file.exists() )
		{
			if(file.delete())
			{
				System.out.println("???????????? ??????"); 
			}
			else{ 
				System.out.println("???????????? ??????"); } 
		}
		
		else{ 
			System.out.println("????????? ???????????? ????????????."); 
		}
		
		service.getCommunityDelete(numb);
         
		return "redirect:/community"; 	
	}
	
	@PostMapping("/community/search")
	@ResponseBody
	public Object getRecipe(@RequestParam(value="text", defaultValue="") String text, HttpSession session) throws ParseException, java.text.ParseException, org.json.simple.parser.ParseException {
		
		Gson gson = new Gson();
		JSONParser jsonParser = new JSONParser();
		Object jarry = new JSONArray();

		List<CommunityVO> communityList = new ArrayList<CommunityVO>();
		
		
		if(text.equals("")) {
			communityList = service.getCommunityList();
			
			
		} else if(text.equals("??????")) {
			communityList = service.getCommunityListSearch(text);
			
		} else if(text.equals("?????????")) {
			communityList = service.getCommunityListSearch(text);
	    	
		} else if(text.equals("?????????")) {
			communityList = service.getCommunityListSearch(text);
	    	
		} else if(text.equals("????????????")) {
			communityList = service.getCommunityListSearch(text);
	
		} else {
			
			communityList = service.getCommunityListinputSearch(text);
			
		}
		
		
		jarry = jsonParser.parse(gson.toJson(communityList));

	
	  	return jarry;
	}
	
	@GetMapping("/rental")
	public String rentalView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
         model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<RentalVO> rentalList = new ArrayList<RentalVO>();
         rentalList = service.getRentalList();
         model.addAttribute("rentalList",rentalList);
         
         
		return "rental"; 	
	}
	
	@GetMapping("/rental_info")
	public String rentalinfoView(@RequestParam("numb") String numb, Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<StudentInfoVO> stulist = new ArrayList<StudentInfoVO>();
         stulist = service.getStuList();
         
         RentalVO com = service.getRentalInfo(numb);
         
         
         model.addAttribute("studentlist", stulist);
         model.addAttribute("com",com);
         
		return "rental_info"; 	
	}
	
	@GetMapping("/rental_write")
	public String rentalWriteView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
		return "rental_write"; 	
	}
	
	
	@PostMapping("/rentalwrite")
	public String rentalWrite(RentalVO vo,
			HttpSession session,
			HttpServletRequest request) throws Exception {
		

		StudentInfoVO student = (StudentInfoVO)session.getAttribute("user");
		vo.setName(student.getName());
		vo.setNum(student.getNum());
		
		
		Date date_now = new Date(System.currentTimeMillis()); // ??????????????? ????????? Date????????? ????????????
		// ?????????????????? 14?????? ??????
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
		String date = fourteen_format.format(date_now);
	
		vo.setDate(date);
			
		service.insertRental(vo);
		return "redirect:/rental";
	}
	
	@PostMapping("/rentaldelete")
	public String rentaldeleteView(@RequestParam("numb") String numb) {	
		
		service.getRentalDelete(numb);
         
		return "redirect:/rental"; 	
	}
	
	@GetMapping("/rentaldone")
	public String rentaldone(@RequestParam("numb") String numb, HttpSession session) {	
	
		StudentInfoVO student = (StudentInfoVO)session.getAttribute("user");
		doneVO done = new doneVO();
		done.setDone_stu(student.getName());
		done.setNumb(numb);
		service.updateDone(done);
         
		return "redirect:/rental"; 	
	}
	
	@PostMapping("/rental/search")
	@ResponseBody
	public Object getRental(@RequestParam(value="text", defaultValue="") String text, HttpSession session) throws ParseException, java.text.ParseException, org.json.simple.parser.ParseException {
		
		Gson gson = new Gson();
		JSONParser jsonParser = new JSONParser();
		Object jarry = new JSONArray();

		List<RentalVO> rentalList = new ArrayList<RentalVO>();
		
		
		if(text.equals("")) {
			rentalList = service.getRentalList();
			
			
		} else if(text.equals("??????")) {
			rentalList = service.getRentalListSearch(text);
			
		} else if(text.equals("??????")) {
			rentalList = service.getRentalListSearch(text);
	    	
		} else {
			
			rentalList = service.getRentalListinputSearch(text);
			
		}
		
		jarry = jsonParser.parse(gson.toJson(rentalList));

	
	  	return jarry;
	}
	
	
	@GetMapping("/itemlist")
	public String itemListView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
         model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         List<ItemVO> itemList = new ArrayList<ItemVO>();
         itemList = service.getItemList();
         model.addAttribute("itemList", itemList);
         
         
		return "itemlist"; 	
	}
	
	
	@GetMapping("/item_write")
	public String itemWriteView(Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
		return "item_write"; 	
	}
	
	@PostMapping("/itemwrite")
	public String itemWrite(ItemVO vo,
			HttpSession session,
			HttpServletRequest request) throws Exception {
		

		StudentInfoVO student = (StudentInfoVO)session.getAttribute("user");
		vo.setStudentname(student.getName());
		vo.setStudentnum(student.getNum());
		
		
		Date date_now = new Date(System.currentTimeMillis()); // ??????????????? ????????? Date????????? ????????????
		// ?????????????????? 14?????? ??????
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
		String date = fourteen_format.format(date_now);
	
		vo.setDate(date);
			
		service.insertItem(vo);
		return "redirect:/itemlist";
	}
	
	
	@GetMapping("/item_info")
	public String iteminfoView(@RequestParam("num") String num, Model model, HttpSession session) {
		StudentInfoVO user = (StudentInfoVO)session.getAttribute("user");
		List<StudentVO> attend = service.getAttendList(user.getNum());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM");
		Calendar calendar = Calendar.getInstance();
		Date dat = calendar.getTime();
		String d_date = dt.format(dat);
		List<StudentVO> attend2 = service.getCheckAlltime(d_date, user.getNum());
		
		for( int i = 0; i <attend.size(); i++){
			if(attend.get(i).getAttend().equals("??????")) {
				int time = Integer.valueOf(attend.get(i).getTime());
				 String hour = String.valueOf((int) Math.floor(time / 60));
		         String minute = String.valueOf(time % 60); 
		         String full = hour + "?????? " + minute +"???";
		         attend.get(i).setTime(full);
			}
		}
		
		int sum = 0;
		for( int i = 0; i <attend2.size(); i++){
				int time = Integer.valueOf(attend2.get(i).getTime());
				sum = sum + time;
		}
		String hour = String.valueOf((int) Math.floor(sum / 60));
        String minute = String.valueOf(sum % 60); 
        String full = hour + "?????? " + minute +"???";
        
        model.addAttribute("t", full);
         StudentInfoVO userSet = service.getStudent(user.getNum());
         model.addAttribute("user",userSet);
         
         ItemVO com = service.getItemInfo(num);
         model.addAttribute("com",com);
         
		return "item_info"; 	
	}
	
	@PostMapping("/itemdelete")
	public String itemdeleteView(@RequestParam("num") String num) {	
		
		service.getItemDelete(num);
         
		return "redirect:/itemlist"; 	
	}
	
	@PostMapping("/item/search")
	@ResponseBody
	public Object itemSearch(@RequestParam(value="text", defaultValue="") String text, HttpSession session) throws ParseException, java.text.ParseException, org.json.simple.parser.ParseException {
		
		Gson gson = new Gson();
		JSONParser jsonParser = new JSONParser();
		Object jarry = new JSONArray();

		List<ItemVO> itemList = new ArrayList<ItemVO>();
		
		
		if(text.equals("")) {
			itemList = service.getItemList();
			
			
		} else if(text.equals("?????????")) {
			itemList = service.getItemListSearch(text);
			
		} else if(text.equals("?????????")) {
			itemList = service.getItemListSearch(text);
	    	
		} else {
			
			itemList = service.getItemListinputSearch(text);
			
		}
		
		jarry = jsonParser.parse(gson.toJson(itemList));

	
	  	return jarry;
	}
	
	@PostMapping("/item/plus")
	@ResponseBody
	public Object itemplus(@RequestParam(value="text", defaultValue="") String text, HttpSession session) throws ParseException, java.text.ParseException, org.json.simple.parser.ParseException {
		
		String jarry = "";
		ItemVO vo = service.getItemInfo(text);
		
		if(vo.getItemcount().equals("")) {
			vo.setItemcount("1");
		}else {
			int number = Integer.parseInt(vo.getItemcount());
			number = number + 1;
			vo.setItemcount(Integer.toString(number));
		}
		
		jarry = vo.getItemcount();
		service.updateItem(vo);
	  	return jarry;
	}

	@PostMapping("/item/minus")
	@ResponseBody
	public Object itemminus(@RequestParam(value="text", defaultValue="") String text, HttpSession session) throws ParseException, java.text.ParseException, org.json.simple.parser.ParseException {
		
		String jarry = "";
		ItemVO vo = service.getItemInfo(text);
		
		if(vo.getItemcount().equals("")) {
			vo.setItemcount("");
		}else {
			int number = Integer.parseInt(vo.getItemcount());
			number = number - 1;
			vo.setItemcount(Integer.toString(number));
		}
		
		jarry = vo.getItemcount();
		service.updateItem(vo);
	  	return jarry;
	}

	
	
	
	
	
}