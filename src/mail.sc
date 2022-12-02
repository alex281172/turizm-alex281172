require: main.sc
require: tour.sc

# отправка сообщения на почту
theme: /SendMail        
    state: Email
        script:
            var ClientMessage = $temp.message;
            $session.result = $mail.sendMessage("alex281172@mail.ru","Заявка от клиента",ClientMessage);
# получение статуса доставки почты
        script:
            $session.MailAnswer = $session.result.status
# отработка вариантов статуса доставки почты
        if: $session.MailAnswer == "OK"
            a: Ваша Заявка отправлена на почту. Вам перезвонит наш менеджер
            a: Если у Вас остались вопросы, то вы можете перезвонить по 8(812)000-00-00
        else:
            a: Во время отправки заявки возникли неполадки. Пожалуйста, для подбора тура позвоните в "Just Tour" по телефону 8(812)000-00-00
            a: У нас много хорошей погоды для Вас!
            
        buttons:
            "Новая заявка"
            "Отмена"
# очистка переменных для новой заявки
        state: AcceptedNew
            q: * (нов*/*заяв*/Новая заявка) *
            script:
                delete $client.city
                delete $client.quantity
                delete $client.children
                delete $client.date
                delete $client.duration
                delete $client.hotelstars
                delete $session.MailAnswer
    
            go!: /Weather/StartWeather


        state: RejectedNew
            q: * (нет/не*/Отмена) *
            a: Позвоните нам снова, у нас всегда хорошая погода! 8(812)000-00-00
