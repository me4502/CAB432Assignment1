const gulp        = require('gulp');
const browserSync = require('browser-sync').create();
const babel       = require('gulp-babel');

// Run babel and move the javascript files into our /src/js folder
gulp.task('babel', function() {
    return gulp.src(['src/babel/*.js'])
        .pipe(babel({
            presets: ['env']
        }))
        .pipe(gulp.dest("../src/main/resources/static/js"))
        .pipe(browserSync.stream());
});

// Static Server + watching scss/html files
gulp.task('serve', ['babel'], function() {

    browserSync.init({
        server: "../src/main/resources/static"
    });

    gulp.watch(['src/babel/*.js'], ['babel']);
    gulp.watch("../src/main/resources/static/*.html").on('change', browserSync.reload);
    gulp.watch("../src/main/resources/static/js/*.js").on('change', browserSync.reload);
});

gulp.task('compile', ['babel']);

gulp.task('default', ['serve']);