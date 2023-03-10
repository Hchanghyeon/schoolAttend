<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
  <meta charset="utf-8">
  <title>QRì¶ì</title>
  <script src="/attend/${path}/resources/js/jsQR.js"></script>
  <meta name="viewport" content="width=device-width, initial-scale=1.0,user-scaleable=no,maximum-scale=1,width=device-width">
  <link href="https://fonts.googleapis.com/css?family=Ropa+Sans" rel="stylesheet">
  <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.3/css/all.css" integrity="sha384-SZXxX4whJ79/gErwcOYf+zWLeJdY/qpuqC4cAa9rOGUstPomtqpuNWT9wdPEn2fk" crossorigin="anonymous">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <link href="/attend/${path}/resources/css/index.css" rel="stylesheet"> 
</head>
<body>
  <div class="arrow"><a onclick='history.go(-1);'><i class="fas fa-arrow-left"></i></a></div>
  <div class="main">
  	<div>
  <h1>QRì¶ì</h1>
  <div id="loadingMessage">ð¥ ì¹ìº ì´ ì°ê²°ëì§ ìììµëë¤ (ìº ì ì°ê²°í´ì£¼ì¸ì)</div>
  <canvas id="canvas" hidden></canvas>
  <div id="output" hidden>
    <div id="outputMessage">QRì½ëê° ì¸ìëì§ ìììµëë¤.</div>
    <div hidden><b>ì¸ìë ê°:</b> <span id="outputData"></span></div>
  </div>
  <div class="cho"><span>ë©ì¸ íë©´ì¼ë¡ ëìê°ê¸°ê¹ì§ </span><span id="time"> </span><span> ì´ ë¨ììµëë¤.</span></div>
    <div id="modal2" class="modal2">
    	<div>
    		<div>
    			<div id="a"></div>
    			<div id="b"></div>
    			<div id="c"></div>
        		<button>íì¸</button>
        	</div>
        </div>
    </div>
    </div>
  </div>
  <script>
	var audio = new Audio("/attend/resources/beep.mp3");
	var audio2 = new Audio("/attend/resources/error.mp3");
    var video = document.createElement("video");
    var canvasElement = document.getElementById("canvas");
    var canvas = canvasElement.getContext("2d");
    var loadingMessage = document.getElementById("loadingMessage");
    var outputContainer = document.getElementById("output");
    var outputMessage = document.getElementById("outputMessage");
    var outputData = document.getElementById("outputData");
    var a = document.getElementById("a");
    var b = document.getElementById("b");
    var c = document.getElementById("c");
    var timeline = document.getElementById("time");
    var time = 20;

    function drawLine(begin, end, color) {
      canvas.beginPath();
      canvas.moveTo(begin.x, begin.y);
      canvas.lineTo(end.x, end.y);
      canvas.lineWidth = 4;
      canvas.strokeStyle = color;
      canvas.stroke();
    }

    // Use facingMode: environment to attemt to get the front camera on phones
    navigator.mediaDevices.getUserMedia({ video: true}).then(function(stream) {
      video.srcObject = stream;
      video.setAttribute("playsinline", true); // required to tell iOS safari we don't want fullscreen
      video.play();
      requestAnimationFrame(tick);
    });
    
    function sound(){
    	audio.play();
    }
    
    function errsound(){
    	audio2.play();
    }
   
    
    function sleep(ms) {
    	  const wakeUpTime = Date.now() + ms;
    	  while (Date.now() < wakeUpTime) {}
    }
    
    
    setInterval(function(){
    	console.log(time);
    	time--;
    	timeline.innerText = time;
    	
    	if(time < 10){
    		$('#time').css("color","red");
    		$('#time').css("fontSize",50);
    	}
    	
    	if(time < 0){
    		time = 30;
    		window.location.href="/attend/choose";
    	}
    }, 1000);
    
    
    
    function tick() {
      loadingMessage.innerText = "â ë¹ëì¤ë¥¼ ë¡ë©ì¤ìëë¤..."
      if (video.readyState === video.HAVE_ENOUGH_DATA) {
        loadingMessage.hidden = true;
        canvasElement.hidden = false;
        outputContainer.hidden = false;

        canvasElement.height = video.videoHeight;
        canvasElement.width = video.videoWidth;
        canvas.drawImage(video, 0, 0, canvasElement.width, canvasElement.height);
        var imageData = canvas.getImageData(0, 0, canvasElement.width, canvasElement.height);
        var code = jsQR(imageData.data, imageData.width, imageData.height, {
          inversionAttempts: "dontInvert",
        });
        
        if (code) {
          
          drawLine(code.location.topLeftCorner, code.location.topRightCorner, "#FF3B58");
          drawLine(code.location.topRightCorner, code.location.bottomRightCorner, "#FF3B58");
          drawLine(code.location.bottomRightCorner, code.location.bottomLeftCorner, "#FF3B58");
          drawLine(code.location.bottomLeftCorner, code.location.topLeftCorner, "#FF3B58");
          outputMessage.hidden = true;
          outputData.parentElement.hidden = false;
          outputData.innerText = code.data;
          console.log(code.data);
          var qrcode = JSON.parse(code.data);
          $.ajax({
				type: "post",
                url: "/attend/check",
                data: {num:qrcode.num, name:qrcode.name, attend:"${value}"},
				success: function(response) {
					console.log(response);
					var response = JSON.parse(response);
					if(response.on == 'success') {
							sound();
						    $('.modal2').css("display","flex");
						    a.innerText = response.name + "ë";
						    b.innerText = response.date + "ì";
						    c.innerText = response.attend + "ëììµëë¤";
						    
						    //.modalìì buttonì í´ë¦­íë©´ .modalë«ê¸°
						    $(".modal2 button").click(function(){
						    	window.location.href="/attend/choose";  	 
						   		$('.modal2').css("display","none");	 	
						    });
						    
						    //.modalë°ì í´ë¦­ì ë«í
						    $(".modal2").click(function (e) {
						  	  if (e.target.className != "modal2") {
						   	   return false;
						    } else {
						    	window.location.href="/attend/choose";  	 
						   	 	$('.modal2').css("display","none");	
						   	 }
						    });
					
					}
					else {
						errsound();
						$('.modal2').css("display","flex");
					    a.innerText = "ìëª» ìë ¥íì¨ìµëë¤."
					    b.innerText = "ë¤ì ëìê°ì"
					    c.innerText = "ì¶ìì²´í¬íì¸ì"
					    	//.modalìì buttonì í´ë¦­íë©´ .modalë«ê¸°
						    $(".modal2 button").click(function(){
						    	window.location.href="/attend/choose";  	 
						   		$('.modal2').css("display","none");	 	
						    });
						    
						    //.modalë°ì í´ë¦­ì ë«í
						    $(".modal2").click(function (e) {
						  	  if (e.target.className != "modal2") {
						   	   return false;
						    } else {
						    	window.location.href="/attend/choose";  	 
						   	 	$('.modal2').css("display","none");	
						   	 }
						    });
					}
					setTimeout(function(){
						window.location.href="/attend/choose";
				    }, 3000);
				},
			    error: function (xhr, ajaxOptions, thrownError) {
			    	errsound();
					$('.modal2').css("display","flex");
				    a.innerText = xhr.status + "ìë¬ ìëë¤.";
				    b.innerText = "ê·¼ë¡ì¥íì ííì´ì§ì ê°ìëìì§ ìì";
				    c.innerText = "íë²ì¼ ì ìì¼ë ì²´í¬ ë¶íëë¦½ëë¤.";
				    	//.modalìì buttonì í´ë¦­íë©´ .modalë«ê¸°
					    $(".modal2 button").click(function(){
					    	window.location.href="/attend/choose";  	 
					   		$('.modal2').css("display","none");	 	
					    });
					    
					    //.modalë°ì í´ë¦­ì ë«í
					    $(".modal2").click(function (e) {
					  	  if (e.target.className != "modal2") {
					   	   return false;
					    } else {
					    	window.location.href="/attend/choose";  	 
					   	 	$('.modal2').css("display","none");	
					   	 }
					    });
						setTimeout(function(){
							window.location.href="/attend/choose";
					    }, 6000);
			    }
			});
	   		return;
        } else {
          outputMessage.hidden = false;
          outputData.parentElement.hidden = true;
        }
          
      }
      
      requestAnimationFrame(tick);
    }
    
	
  </script>
</body>
</html>
