function previewFile1(input){
	alert("here1");
	var file = $("#fileinput1").prop("files")[0];
    if(file){
        var reader = new FileReader();
        reader.onload = function(){
            $("#previewSign1").attr("src", reader.result);
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
