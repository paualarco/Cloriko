function UserLogin() {
        var n = document.getElementById("username").value;
        var p = document.getElementById("password").value;

        var postObj = JSON.stringify({
            "username": n,
            "password": p
        });
        alert(postObj)
        var xhttp = new XMLHttpRequest();   // new HttpRequest instance
        xhttp.open("POST", "http://localhost:8080/signIn");
        xhttp.setRequestHeader("Content-Type", "application/json");
        xhttp.onreadystatechange = function() {
        alert(xhttp.status)
        console.log(xhttp.status)
        if (this.readyState == 4 && this.status == 200) {
            console.log(xhttp.responseText);
        }
    };
        xhttp.send(postObj);
}

function SignUp() {
var username = document.getElementById("username").value;
var password = document.getElementById("password").value;
var name = document.getElementById("name").value;
var last_name = document.getElementById("last_name").value;
var email = document.getElementById("email").value;

var postObj = JSON.stringify({
"username": username,
"password": password,
"name": name,
"last_name": last_name,
"email": email
});
alert(postObj)
var xhttp = new XMLHttpRequest();   // new HttpRequest instance
xhttp.open("POST", "http://localhost:8080/signUp");
xhttp.setRequestHeader("Content-Type", "application/json");
xhttp.onreadystatechange = function() {
alert(xhttp.readyState)
       window.loca location = "http://stackoverflow.com";

console.log(xhttp.status)
if (this.readyState == 4 && this.status == 201) { //response created
console.log(xhttp.responseText);
}
};
xhttp.send(postObj);
}