/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



$.get("ServletYoutubeChannel", {}, function (youtubeChanHtml) {
    var a = document.createElement('a');
    var linkText = document.createTextNode("My Youtube Channel");
    a.appendChild(linkText);
    a.title = "My Youtube Channel";
    a.href = youtubeChanHtml;
    var element = document.getElementById("chanlink");
    element.appendChild(a);
});


