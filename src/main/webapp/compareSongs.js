/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


$(document).bind("pageinit", function ()
{    
    $.post('ServletCompareSongs', {},function(data)
    {
        comparisonScore = jQuery.parseJSON(data);
        document.getElementById("comparisonScore").value = comparisonScore;
    })
})
