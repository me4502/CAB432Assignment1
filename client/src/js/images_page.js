import {API_URL, checkForStorage} from "./util"

export function populateImages() {
    const children = document.querySelector("#image-container");

    while (children.hasChildNodes()) {
        children.removeChild(children.lastChild);
    }

    const search = document.querySelector("#search_bar").value;

    window.fetch(API_URL + '/image/search/' + search)
        .then(res => res.json())
        .then(json => {
            for (let url in json) {
                url = json[url];
                const div = `<div class="col s12 m4">
                      <div class="card">
                        <div class="card-image">
                          <img src="${url['url']}">
                        </div>
                        <div class="card-action">
                          <a href="#" onclick="main.moveToTagPage('${url['id']}', '${url['url']}')">Choose</a>
                        </div>
                      </div>
                    </div>`;
                let child = document.createElement('div');
                child.innerHTML = div;
                children.appendChild(child);
            }
        }).catch(ex => {
        children.innerHTML = "<p>Failed to search: " + ex.message +"</p>";
        console.log('Failed to parse', ex)
    });
}

export function submitSearchBar(e) {
    if (e.which === 13) { // Enter Key (There has to be some constant right?)
        populateImages();
        e.preventDefault();
    }
}

export function moveToTagPage(imageId, url) {
    localStorage.setItem("chosenImage", imageId);
    localStorage.setItem("chosenImageURL", url);
    window.location.href = "/select_tags";
}
