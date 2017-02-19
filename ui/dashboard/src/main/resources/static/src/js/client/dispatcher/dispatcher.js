var appDispatcher = {
    handle: function (req, callback) {
        var request = $.ajax({
            url: req.url,
            method: req.method,
            data: req.body,
            dataType: "json",
            contentType: req.contentType,
            processData: false
        })
            .success(callback)
            .error(function (err) {
                $(".modal").modal();
                if (err.statusCode().status == 404) {
                    $(".modal-body").text("The requested resource was not found");
                    callback(err);
                } else {
                    $(".modal-body").text($.parseJSON(err.responseText).message);
                }
            });
    }
};

function generate(method, body, url) {
    return {
        method: method,
        body: body,
        url: url,
        contentType: 'application/json'
    }
}