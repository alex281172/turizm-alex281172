require: dateTime/dateTime.sc
    module = sys.zb-common

require: localPattern.sc

require: mail.sc



theme: /Tour
    state: Start
        a: ОФОРМЛЕНИЕ ЗАЯВКИ:
        a: Вас зовут {{$client.name}}, ваш телефон {{$client.phone}}.
        go!: /Tour/AskQuantity

# модуль запроса количества туристов
    state: AskQuantity

# проверка на наличие данных по количеству взрослых туристов и переход внесение данных о количества детей
        if: $client.quantity
            go!: /Tour/AskQuantityChildren
# кнопки с вариантами выбора
        a: Укажите количество взрослых в поездке ->:
        buttons:
            "1"
            "2"
            "3"
            "4"
            "5"
            "не знаю"
            "отмена"
# указание количества взрослых в соответствии с паттерном и переход на подтверждение введенного количества взрослых
        state: Get
            q: $quantity
            script:
                log("!!!!OLDER!!!!" + toPrettyString($parseTree));
            a: отлично
            go!: /Tour/ConfirmQuantity
# отработка ошибки формата ввода количества взрослых             
        state: Wrong
            q: *
            a: какая-то ошибка
            go!: /Tour/AskQuantity  
# отработка сценариев разных вариантов количеста взрослых туристов: не знаю, отмена и числа
# если "не знаю" или число, то переход на следующий стейт, если "отмена" удаление данных и переход StartWeather
    state: ConfirmQuantity
        script:
            $session.quantity = $parseTree._quantity;
            $client.quantity = $session.quantity;
            delete $session.quantity;
        if: $client.quantity == "не знаю"
            a: Вы сказали "{{ $client.quantity }}". Уточнить количество можно позже
        elseif: $client.quantity == "отмена"   
            a: Вы сказали "{{ $client.quantity }}"
            script:
                delete $client.quantity
            go!: /Weather/StartWeather             
        else:
            a: Вы указали: взрослых {{ $client.quantity }} чел.
# если "не знаю" или число, то переход на следующий стейт AskQuantityChildren
        go!: /Tour/AskQuantityChildren


# модуль запроса количества детей
    state: AskQuantityChildren
# проверка на наличие данных по количеству детей и переход на внесение данных о дате тура            

        if: $client.children
            go!: /Tour/AskTourDate
# кнопки с вариантами выбора
        a: Укажите количество детей (до 16лет) в поездке ->:
        buttons:
            "0"
            "1"
            "2"
            "3"
            "4"
            "не знаю"
            "отмена"
# указание количества детей в соответствии с паттерном и переход на подтверждение введенного количества детей
        state: Get
            q: $quantity
            script:
                log("!!!!Children!!!!!" + toPrettyString($parseTree));
            a: отлично
            go!: /Tour/ConfirmQuantityChildren
            
# отработка ошибки формата ввода количества взрослых             
        state: Wrong
            q: *
            a: какая-то ошибка
            go!: /Tour/AskQuantityChildren    
# отработка сценариев разных вариантов количеста детей: "не знаю", "отмена" и числа
# если "не знаю" или число, то переход на следующий стейт, если "отмена" удаление данных и переход StartWeather

    state: ConfirmQuantityChildren
        script:
            $session.quantity_children = $parseTree._quantity;
            $client.children = $session.quantity_children;
            delete $session.quantity_children;

        if: $client.children == "не знаю"
            a: Вы сказали "{{ $client.children }}". Уточнить количество детей можно позже
           
        elseif: $client.children == "отмена"   
            a: Вы сказали "{{ $client.children }}"
            script:
                delete $client.children
            go!: /Weather/StartWeather            
        else:
            a: Вы указали: детей {{ $client.children }}.
# если "не знаю" или число, то переход на следующий стейт AskTourDate
        go!: /Tour/AskTourDate



# модуль запроса даты туры
    state: AskTourDate
# проверка на наличие внесенных ранее данных и переход на следующий стейт
        if: $client.date
            go!: /Tour/AskTourDuration      
# получение текущей даты для напоминания туристу 
        script:
            $temp.date = currentDate();
            $session.date_curr = $temp.date.locale("ru").format("DD/MM/YYYY");
            
        a: Сегодня {{$session.date_curr}}. Укажите дату начала поездки в формате DD/MM/YYYY >:
# вывод кнопок с варинатами ответа кроме даты
        buttons:
            "не знаю"
            "отмена"
# получение даты начала тура в формате в соответствии с паттерном дат
        state: Get
            q: $tourdate
            script:
                log("!!!!!!-----$tourdate-----!!!!!!!!!" + toPrettyString($parseTree));
#  если корректно, то переход на следующий стейт ConfirmTourDate
            go!: /Tour/ConfirmTourDate
# отработка ошибки формата введения даты
        state: Wrong
            q: *
            a: укажите правильную дату
# переход на предыдущий стейт
            go!: /Tour/AskTourDate    

# стейт отработки введенной информации о дате тура
    state: ConfirmTourDate
        script:
            $session.date = $parseTree._tourdate;
            $client.date = $session.date;
            delete $session.date;

# если "не знаю" или дата, то переход на следующий стейт AskTourDuration

        if: $client.date == "не знаю"
            a: Вы сказали "{{ $client.date }}". Начало поздки можно уточнить позже            
            go!: /Tour/AskTourDuration
# если "отмена" удаление данных и переход StartWeather
        elseif: $client.date == "отмена"   
            a: Вы сказали "{{ $client.date }}"
            script:
                delete $client.date
            go!: /Weather/StartWeather
             
        else:
            go!: /Tour/AskTourDuration            

# получение продолжительности тура в формате в соответствии с паттерном продолжительности тура
    state: AskTourDuration
# проверка на наличие внесенных ранее данных и переход на следующий стейт
        if: $client.duration
            go!: /Tour/AskHotelStars    
# вывод кнопок с вариантами ответа 
        a: Укажите продолжительность поездки в днях>:
        buttons:
            "5"
            "7"
            "9"
            "14"
            "не знаю"
            "отмена"
        state: Get
            q: $tourduration
            script:
                log("!!!!" + toPrettyString($parseTree));
            go!: /Tour/ConfirmTourDuration
        
        state: Wrong    
            q: *
            go!: /Tour/AskTourDuration
# отработка сценариев разных вариантов продолжительности тура: "не знаю", "отмена" и числа
# если "не знаю" или число, то переход на следующий стейт, если "отмена" удаление данных и переход StartWeather            
    state: ConfirmTourDuration
        script:
            $session.tour_duration = $parseTree._tourduration;
            $client.duration = $session.tour_duration;
            delete $session.tour_duration;

# если "не знаю" или дата, то переход на следующий стейт AskHotelStars
        if: $client.duration == "не знаю"
            a: Вы сказали "{{ $client.duration }}". Продолжительнсть поздки можно уточнить позже            
            go!: /Tour/AskHotelStars
# если "отмена" удаление данных и переход StartWeather
        elseif: $client.duration == "отмена"   
            a: Вы сказали "{{ $client.duration }}"
            script:
                delete $client.duration
            go!: /Weather/StartWeather
             
        else:
            a: Продолжительность поездки {{$client.duration}} дн.
            go!: /Tour/AskHotelStars
            
# получение звезд отеля в формате в соответствии с паттерном звездности            
    state: AskHotelStars
# проверка на наличие внесенных ранее данных и переход на следующий стейт
        if: $client.hotelstars
            go!: /Tour/AskNote          
        q: $hotelstars
# вывод кнопок с вариантами ответа 
        a: Укажите звездность отеля ->:
        buttons:
            "2"
            "3"
            "4"
            "5"
            "не знаю"
            "отмена"
        state: Get
            q: $hotelstars
            script:
                log("!!!!" + toPrettyString($parseTree));
            go!: /Tour/ConfirmHotelStars
            
        state: Wrong    
            q: *
            go!: /Tour/AskHotelStars
# отработка сценариев разных вариантов отелей: "не знаю", "отмена" и числа
# если "не знаю" или число, то переход на следующий стейт, если "отмена" удаление данных и переход StartWeather              
    state: ConfirmHotelStars
        
        script:
            $session.tour_hotelstars = $parseTree._hotelstars;
            $client.hotelstars =  $session.tour_hotelstars;
            delete $session.tour_hotelstars;
# если "не знаю" или дата, то переход на следующий стейт AskNote       
   
        if: $client.hotelstars == "не знаю"
            a: Вы сказали "{{ $client.hotelstars }}". Отель можно уточнить позже            
            # go!: /Tour/AskNote
# если "отмена" удаление данных и переход StartWeather
        elseif: $client.hotelstars == "отмена"   
            a: Вы сказали "{{ $client.hotelstars }}"
            script:
                delete $client.hotelstars
            go!: /Weather/StartWeather
             
        else:
            a: Вы выбрали {{$client.hotelstars}}* отель
        go!: /Tour/AskNote 
        
# получение заметки для менеджера         
    state: AskNote
        a: Вы можете оставить комментарий для менеджера и уточнить желаемый бюджет тура
        state: Get
            q: $note
            script:
                log("!!!!NOTE!!!!" + toPrettyString($parseTree));
                $session.tour_note = $parseTree._note;
                $client.note = $session.tour_note;
                delete $session.tour_note;
                
            go!: /Tour/ConfirmNote
          
# подтверждение полученных данных для заментки 
    state: ConfirmNote
        a: Ваша заметка для менеджера: {{$client.note}}?
# вывод кнопок с вариантами ответа
        buttons:
            "да"
            "изменить"
            "отмена"  
# отработка ответов
# если "да" переход к слудующему стейту Final
        state: AcceptedNote
            q: (да/верно)
            a: Отправляется запрос нашему менеджеру
            go!: /Note/Final
    # если "нет" то удаление данных и переход StartWeather
        state: RejectedNote
            q: * (нет/отмена) *
            script:
                delete $client.note
            go!: /Weather/StartWeather
    # если изменить, то переход в начало тек стейта
        state: ChangeNote
            q: * (измен*) *
            script:
                delete $client.note
            go!: /Tour/AskNote


theme: /Note
# стейт сбора данных и формирование текста сообщения
    state: Final        
        script:
            $session.MychatId = $request.channelUserId;
            $temp.message = "ЗАЯВКА на подбор тура: " + "Имя туриста " + $client.name + "; контактный телефон: " + 
            $client.phone + "; Город/Страна: " + $client.city +
            "; взрослых: " + $client.quantity + "; детей: " + $client.children +
            "; дата начала поездки: " + $client.date  + "; продолжительность: " +  $client.duration  +
            "; класс отеля: " + $client.hotelstars + "; заметка для менеджера: " +  $client.note
            $temp.message1 = $request.channelUserId;
     
            $session.tour_message = $temp.message
            $client.tour_message = $session.tour_message
        a: {{$temp.message}}
        
# переход на отправку почты
        go!: /SendMail/Email

