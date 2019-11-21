function setNotify() {
    var login = atob(getCookie()).split(":")[0];
    var message = $("#message").val();

    $.post("/railway_station_service_war_exploded/services/manager/secured/send_notify", {
        login: login,
        message: message
    }, function () {
        alert("Notification message is created");
        window.location.replace("/railway_station_service_war_exploded/manager.html");
    }).fail( function () {
        alert("Notification message is not created")
    });
}

let employeeData;
let trainData;
function createListOfEmployees(items) {
    // $("#employee").show();
    // $("#trains").hide();
    // $("#main-block").hide();
    employeeData = items;
    let str = "<table class=\"table table-bordered\">\n" +
        "                        <thead class=\"thead-dark\">\n" +
        "                        <tr>\n" +
        "                            <th scope=\"col\">First&nbspname</th>\n" +
        "                            <th scope=\"col\">Last&nbspname</th>\n" +
        "                            <th scope=\"col\">Salary</th>\n" +
        "                            <th scope=\"col\">First&nbspday&nbspat&nbspwork</th>\n" +
        "                            <th scope=\"col\">Working hours</th>\n" +
        "                            <th scope=\"col\">Pay&nbspcheck</th>\n" +
        "                        </tr>\n" +
        "                        </thead>\n" +
        "                        <tbody id=\"manager-agents\" >";
    for (let i=0; i<items.length; i++) {
        str +="<tr id=\"" + i + "\"><th scope=\"row\">"+items[i].first_name+"</th><td>"+ items[i].last_name +"</td><td>"+items[i].salary + "</td>";
        str +="<td>" + items[i].email + "</td><td>" + items[i].workingHours + "</td>";
        str +="<td><button type=\"submit\" onclick ='payroll(" + i +");' class=\"btn btn-primary\">Paycheck</button></td></tr>";
    }
    str += "</tbody></table>";
    $("#main-block").html("");
    $("#main-block").append(str);
}

function cancelRoute(index) {
    //To do
    //Take all required data from trainData and remove which is required
    alert("route is cancelled!");
}

function createListOfTrains(items) {
    // $("#employee").hide();
    // $("#main-block").hide();
    // $("#trains").show();
    trainData = items;
    let str = "<table class=\"table table-bordered\">\n" +
        "                        <thead class=\"thead-dark\">\n" +
        "                        <tr>\n" +
        "                            <th scope=\"col\">Train Name</th>\n" +
        "                            <th scope=\"col\">Departure city</th>\n" +
        "                            <th scope=\"col\">Arrival city</th>\n" +
        "                            <th scope=\"col\">Departure time</th>\n" +
        "                            <th scope=\"col\">Departure date</th>\n" +
        "                            <th scope=\"col\">Cancel route</th>\n" +
        "                        </tr>\n" +
        "                        </thead>\n" +
        "                        <tbody id=\"manager-trains\">";
    for (let i=0; i<items.length; i++) {
        let date = items[i][2].split(" ");
        str +="<tr id=\"" + i + "\"><th scope=\"row\">"+items[i][0]+"</th><td>"+ items[i][1] +"</td><td>"+items[i][3] + "</td>";
        str +="<td>" + date[1].slice(0, -2) + "</td><td>" + date[0] + "</td>";
        str +="<td><button type=\"submit\" onclick ='cancelRoute(" + i +");' class=\"btn btn-primary\">Cancel ticket</button></td></tr>";
    }
    str += "</tbody></table>";
    $("#main-block").html("");
    $("#main-block").append(str);
}

function payroll(index) {
    $.ajaxSetup({
        headers:{
            'Authorization': "Basic " + getCookie()
        }
    });
    let items = employeeData[index];
    let url = "/railway_station_service_war_exploded/services/manager/payroll/" + items['email'] + "/" + items['salary'];
    $.ajax({
        type: "POST",
        url: url,
        success: function () {
        },
    });
}

function getUserData() {

    $.ajaxSetup({
        headers:{
            'Authorization': "Basic " + getCookie()
        }
    });

    if(typeof $.cookie('encrypted') === "undefined"){
        console.log("Cookie doesn't exists");
    } else{
        $.post("/railway_station_service_war_exploded/services/manager/secured/managerProfile", {
            authToken: getCookie()
        }, function (out) {
            let data = JSON.parse(out);
            let first_name = data['first_name'];
            let last_name = data['last_name'];
            let phone = data['phone'];
            let email = data['email'];

        })
    }
}

function getAllPaychecks() {

    $.ajaxSetup({
        headers:{
            'Authorization': "Basic " + getCookie()
        }
    });
    let email = atob(getCookie()).split(":")[0];
    let url = "/railway_station_service_war_exploded/services/manager/secured/payCheckList/" + email;
    $.ajax({
        type: "GET",
        url: url,
        success: function () {
            window.location.replace("paycheck.html");
        },
    });
}

function ListOfEmployees() {
    let url = "/railway_station_service_war_exploded/services/manager/secured/listOfEmployees";

    $.ajaxSetup({
        headers:{
            'Authorization': "Basic " + getCookie()
        }
    });

    $.ajax({
        type: "GET",
        url: url,
        success: function (data) {
            createListOfEmployees(JSON.parse(data));
        },
    });

}

function notify_form() {
    let str = "    <div class=\"card\">\n" +
        "        <div class=\"card-header\">\n" +
        "            <h5>Notification</h5>\n" +
        "        </div>\n" +
        "        <div class=\"card-body\"><div class=\"input-group input-group-lg\">\n" +
        "  <input type=\"text\" class=\"form-control\" aria-label=\"Large\" aria-describedby=\"inputGroup-sizing-sm\">\n" +
        "</div><button type=\"button\" class=\"btn btn-primary\" id=\"\">Submit</button></div>\n" +
        "    </div>\n" +
        "</div>";
    $("#main-block").html(str);
}

function readTextFile() {
    $.ajax({
        type: 'GET',
        url: "/railway_station_service_war_exploded/services/items/getLogs",
        success: function(out) {
            out = out.replace("\"","");
            out = out.replace("\"","");
            console.log(out);
            let arr = out.split("\\n");
            let str = "";
            for(let i=0; i<arr.length; i++){
                str+= "<p class='logs_text'>" + arr[i] +"</p>"
                if(arr[i].indexOf("ALMT") !== -1){
                    str += "</br>";
                }
            }
            $("#logs").append(str);
        },
        fail: function(err) {
            console.log(err);
        },
        contentType: "application/json"
    })
}

function listAllTrains() {
    let url = "/railway_station_service_war_exploded/services/manager/secured/listOfTrains";

    $.ajaxSetup({
        headers:{
            'Authorization': "Basic " + getCookie()
        }
    });

    $.ajax({
        type: "GET",
        url: url,
        success: function (data) {
            console.log(data);
            createListOfTrains(JSON.parse(data));
        },
    });
}

function logging(){
    if( document.getElementById("startLogs").checked === true){
        readTextFile();
    } else{
        $("#logs").html("");
    }
}

$(document).ready(function () {
    // getUserData();

    getProfile();
    ListOfEmployees();
    $("#makePayment").on('click', function () {
        ListOfEmployees();
    });
    $("#paycheck").on('click', function () {
        getAllPaychecks();
    });
    $("#notify-button").on('click', function () {
        notify_form();
    });

    $("#look-agent").on('click', function () {
        ListOfEmployees();
    })
    $("#look-routes").on('click', function () {
        listAllTrains();
    })
});