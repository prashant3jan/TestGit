/**
 * 
 */
function previewFile1(input){
	alert("here1");
	var file = $("#fileinput1").prop("files")[0];
    if(file){
        var reader = new FileReader();
        reader.onload = function(){
            $("#previewImg1").attr("src", reader.result);
        }
        reader.readAsDataURL(file);
    }
}

function previewFile2(input){
	alert("here2");
    var file = $("#fileinput2").prop("files")[0];
    if(file){
        var reader = new FileReader();
        reader.onload = function(){
            $("#previewSign2").attr("src", reader.result);
        }
        reader.readAsDataURL(file);
    }
}
function previewFile3(input){
	alert("here3");
	var file = $("#fileinput3").prop("files")[0];
    if(file){
        var reader = new FileReader();
        reader.onload = function(){
            $("#previewSign3").attr("src", reader.result);
        }
        reader.readAsDataURL(file);
    }
}
function previewFile4(input){
	alert("here4");
    var file = $("#fileinput4").prop("files")[0];
    if(file){
        var reader = new FileReader();
        reader.onload = function(){
            $("#previewSign4").attr("src", reader.result);
        }
        reader.readAsDataURL(file);
    }
}
    function previewFile5(input){
    	alert("here4");
        var file = $("#fileinput5").prop("files")[0];
        if(file){
            var reader = new FileReader();
            reader.onload = function(){
                $("#previewSign5").attr("src", reader.result);
            }
            reader.readAsDataURL(file);
        }
}
    
