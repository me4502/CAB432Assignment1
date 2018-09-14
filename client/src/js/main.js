import 'whatwg-fetch'

const API_URL = 'http://localhost:5078';

export function populateImages() {
    const children = document.querySelector("#image-container");

    while (children.hasChildNodes()) {
        children.removeChild(children.lastChild);
    }

    const search = document.querySelector("#search_bar").value;

    window.fetch(API_URL + '/image/search/' + search)
        .then(function(response) {
            return response.json();
        }).then(function(json) {
            for (let url in json) {
                const div = `<div class="row">
                    <div class="col s12 m7">
                      <div class="card">
                        <div class="card-image">
                          <img src="${url['url']}">
                        </div>
                        <div class="card-action">
                          <a href="#">Choose Image</a>
                        </div>
                      </div>
                    </div>
                  </div>`;
                let child = document.createElement('div');
                child.innerHTML = div;
                children.appendChild(child);
            }
        }).catch(function(ex) {
            children.innerHTML = "<p>Failed to search: " + ex.message +"</p>";
            console.log('Failed to parse', ex)
        });
}