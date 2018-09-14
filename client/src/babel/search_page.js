function populateImages() {
    const children = document.querySelector("#image-container");

    while (children.hasChildNodes()) {
        children.removeChild(children.lastChild);
    }

    const search = document.querySelector("#search_bar").value;

    for (let url in urls) {
        const div = `<div class="row">
            <div class="col s12 m7">
              <div class="card">
                <div class="card-image">
                  <img src="${url}">
                </div>
                <div class="card-action">
                  <a href="#">Choose Image</a>
                </div>
              </div>
            </div>
          </div>`
    }
}