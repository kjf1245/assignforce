/*

    utilService provides generalized functions
 */

app.service('utilService', function(){
    const util = {
        day:
            {
                oneDayInMs: 1000 * 60 * 60 * 24,
                trimDate: (day) => {
                    return new Date(day.getFullYear(), day.getMonth(), day.getDate())
                },
                addDays: (day, count) => {
                    return new Date(day.getTime() + (util.day.oneDayInMs * count))
                },
                inRange: (date, start, end) => {
                    return (start <= date && date <= end);
                },
                daySequence: (sd, ed) => {
                    if (!sd || !ed || ed.getTime() - sd.getTime() <= 0)
                        return [];
                    const start = util.day.trimDate(sd);
                    const end = util.day.trimDate(ed);
                    let day = start;
                    let days = [];
                    while (day <= end) {
                        days.push(day);
                        day = util.day.addDays(day, 1);
                    }
                    return days;
                }
            }
    };


    return util;
});