const deleteButton = document.getElementById('delete-btn');
const modifyButton = document.getElementById('modify-btn');
const createButton = document.getElementById('create-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', (event) => {
        let id = document.getElementById('article-id').value;

        const isUserConfirmed = confirm('정말 삭제하시겠습니까?');
        if (!isUserConfirmed) {
            return false;
        }
        function success() {
            alert("삭제가 완료되었습니다.");
            location.replace("/articles");
        }
        function fail() {
            alert("삭제 실패하였습니다.");
            location.replace("/articles");
        }
        httpRequest("DELETE", "api/articles/" + id, null, success, fail);
    });
}

if (modifyButton) {
    modifyButton.addEventListener('click', (event) => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        const isUserConfirmed = confirm('수정 하시겠습니까?');
        if (!isUserConfirmed) {
            return false;
        }

        body: JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        });

        function success() {
            alert("수정 완료되었습니다.");
            location.replace("/articles/" + id);
        }
        function fail() {
            alert("수정 실패했습니다.");
            location.replace("/articles/" + id);
        }
        httpRequest("PUT", "api/articles/" + id, body, success, fail);
    });

}

if (createButton) {
    createButton.addEventListener('click', (event) => {

        body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value,
        });
        function success() {
            alert("등록 완료되었습니다.");
            location.replace("/articles");
        }
        function fail() {
            alert("등록 실패했습니다.");
            location.replace("/articles");
        }

        httpRequest("POST", "api/articles", body, success, fail);
    });
}

function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(";");
    cookie.some(function (item) {
        item = item.replace(" ", "");

        var dic = item.split("=");

        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });
    return result;
}

function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            Authorization: "Bearer "+ localStorage.getItem("access_token"),
            "Content-Type": "application/json",
        },
        body: body,
    })
    .then((response) => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if (response.status === 401 && refresh_token) {
            fetch("api/token", {
                    method: "POST",
                    headers: {
                        Authorization: "Bearer "+ localStorage.getItem("access_token"),
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        refreshToken: getCookie("refresh_token"),
                    }),
            })
            .then((res) => {
                if (res.ok) {
                    return res.json();
                }
            })
            .then((result) => {
                localStorage.setItem("access_token", result.accessToken);
                httpRequest(method, url, body, success, fail);
            })
            .catch((error) => fail());
        } else {
            return fail();
        }
    });
}


//        const isUserConfirmed = confirm('게시 하시겠습니까?');
//        if (!isUserConfirmed) {
//            return false;
//        }

//        fetch(`/api/articles`, {
//            method: 'POST',
//            headers: {
//                "Content-Type": "application/json",
//            },
//            body: JSON.stringify({
//                title: document.getElementById('title').value,
//                content: document.getElementById('content').value
//            })
//        })
//        .then(() => {
//            alert('게시가 완료되었습니다.');
//            location.replace('/articles');
//        });

