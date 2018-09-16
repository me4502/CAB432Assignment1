const gulp        = require('gulp');
const browserSync = require('browser-sync').create();
const webpack     = require('webpack-stream');


// Run babel and move the javascript files into our /src/js folder
gulp.task('transpile', function() {
    return gulp.src(['src/js/main.js'])
        .pipe(webpack({
            mode: 'development',
            devtool : 'source-map',
            output: {
                filename: 'packed.js',
                library: 'main'
            },
            module: {
                rules: [
                    {
                        test: /\.js$/,
                        exclude: /(node_modules|bower_components)/,
                        use: {
                            loader: 'babel-loader',
                            options: {
                                presets: ['@babel/preset-env']
                            }
                        }
                    }
                ]
            }
        }))
        .pipe(gulp.dest("../src/main/resources/static/js"))
        .pipe(browserSync.stream());
});

gulp.task('watch_compile', ['transpile'], function() {
    gulp.watch(['src/js/*.js'], ['transpile']);
});

// Static Server + watching scss/html files
gulp.task('serve', ['transpile'], function() {

    browserSync.init({
        server: "../src/main/resources/static"
    });

    gulp.watch(['src/js/*.js'], ['transpile']);
    gulp.watch("../src/main/resources/static/*.html").on('change', browserSync.reload);
    gulp.watch("../src/main/resources/static/js/*.js").on('change', browserSync.reload);
});

gulp.task('compile', ['transpile']);

gulp.task('default', ['serve']);