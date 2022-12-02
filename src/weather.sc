require: functions.js
require: tour.sc
require: localPattern.sc

theme: /Weather
    state: StartWeather
# проверка на наличие данных ранее совершенного поиска
        if: $client.city
            a: Вы уже смотрели погоду в {{$client.city}}. Продолжим оформлять заявку на тур или посмотрим погоду в другом месте?
            buttons:
                "Да"
                "Погода"
                "Нет"
# отработка выбора да - продолжить, погода - новый поиск погоды, отмена - 
        state: Accepted
            q: * (да/давай*/хорошо/ок*) *
            go!: /Tour/Start            
        state: Weather
            q: * (Погода) *
            script:
                delete $client.city
                delete $client.quantity
                delete $client.children
                delete $client.data
                delete $client.duration
                delete $client.hotelstars
            go!: /Weather/StartWeather1
        state: Rejected
            q: * (нет/не*) *
            a: Как жаль! У нас так много хорошей погоды для Вас...
            go!: /Welcome
        else:
            go!: /Weather/StartWeather1
            
    state: StartWeather1
        q!: $weather        
        a: Я могу сообщить вам текущую погоду в любом городе. Укажите город

    state: GetWeather
        intent!: /geo
        script:
            var city = $caila.inflect($parseTree._geo, ["nomn"]);
            $session.tour_city = capitalize(city)
            $client.city = $session.tour_city
            delete $session.tour_city
            openWeatherMapCurrent("metric", "ru", city).then(function (res) {
                if (res && res.weather) {
                    $reactions.answer("Сегодня в городе " + capitalize(city) + " " + res.weather[0].description + ", " + Math.round(res.main.temp) + "°C" );
                    if(Math.round(res.main.temp) > 29) {
                        $reactions.answer("Очень жарко в " + capitalize(city) + ". Подберем другой город?")
                    } 
                    
                    else if (Math.round(res.main.temp) < 15) {
                        $reactions.answer("Что-то прохладно. Вы точно хотите поехать в " + capitalize(city) + "?")
                    }
                    
                    else {
                        $reactions.answer("Отличная погода. Хороший выбор! Оформим тур в " + capitalize(city) + "?")
                    }                    
                    
                } else {
                    $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                }
                
                
            }).catch(function (err) {
                $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
            });
        buttons:
            "Оформление тура"
            "Погода"
            "Нет"
        state: Accepted
            q: * (да/давай*/хорошо/ок*/оформление тура/* тур*) *
            go!: /Tour/Start
        state: Weather
            q: * (Погода) *
            go!: /Weather/StartWeather1
        state: Rejected
            q: * (нет/не*) *
            a: Как жаль! У нас так много хорошей погоды для Вас...
            a: Если у Вас остались вопросы, то вы можете перезвонить по 8(812)000-00-00
            
            
        
            
    state: CatchAll || noContext=true
        event!: noMatch
        a: Извините, я вас не понимаю, зато могу рассказать о погоде. Введите название города
        go: /GetWeather
