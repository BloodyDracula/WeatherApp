<template>
  <div class="flex flex-col flex-1 items-center">
    <!-- Баннер -->
    <div
        v-if="route.query.preview"
        class="text-white p-4 bg-weather-secondary w-full text-center"
    >
      <p>
        В данный момент вы просматриваете этот город, нажмите + чтобы начать его отслеживать
      </p>
    </div>
    <!-- Обзор погоды -->
    <div class="flex flex-col items-center text-white py-12">
      <h1 class="text-4xl mb-2">{{ route.params.city }}</h1>
      <p class="text-sm mb-12">
        {{
          new Date(weatherData.currentTime).toLocaleDateString(
              "ru-ru",
              {
                weekday: "short",
                day: "2-digit",
                month: "long",
              }
          )
        }}
        {{
          new Date(weatherData.currentTime).toLocaleTimeString(
              "ru-ru",
              {
                timeStyle: "short",
              }
          )
        }}
      </p>
      <p class="text-8xl mb-8">
        {{ Math.round(weatherData.current.temp) }}&deg;
      </p>
      <p>
        Ощущается как
        {{ Math.round(weatherData.current.feels_like) }} &deg;
      </p>
      <p class="capitalize">
        {{ weatherData.current.weather[0].description }}
      </p>
      <img
          class="w-[150px] h-auto"
          :src="
          `http://openweathermap.org/img/wn/${weatherData.current.weather[0].icon}@2x.png`
        "
          alt=""
      />
    </div>

    <hr class="border-white border-opacity-10 border w-full" />

    <!-- Почасовая погода -->
    <div class="max-w-screen-md w-full py-12">
      <div class="mx-8 text-white">
        <h2 class="mb-4">Почасовая погода</h2>
        <div class="flex gap-10 overflow-x-scroll">
          <div
              v-for="hourData in weatherData.hourly"
              :key="hourData.dt"
              class="flex flex-col gap-4 items-center"
          >
            <p class="whitespace-nowrap text-md">
              {{
                new Date(
                    hourData.currentTime
                ).toLocaleTimeString("ru-ru", {
                  hour: "2-digit",
                })
              }}
            </p>
            <img
                class="w-auto h-[50px] object-cover"
                :src="
                `http://openweathermap.org/img/wn/${hourData.weather[0].icon}@2x.png`
              "
                alt=""
            />
            <p class="text-xl">
              {{ Math.round(hourData.temp) }}&deg;
            </p>
          </div>
        </div>
      </div>
    </div>

    <hr class="border-white border-opacity-10 border w-full" />

    <!-- Недельная погода -->
    <div class="max-w-screen-md w-full py-12">
      <div class="mx-8 text-white">
        <h2 class="mb-4">Прогноз погоды на 7 дней</h2>
        <div
            v-for="day in weatherData.daily"
            :key="day.dt"
            class="flex items-center"
        >
          <p class="flex-1">
            {{
              new Date(day.dt * 1000).toLocaleDateString(
                  "ru-ru",
                  {
                    weekday: "long",
                  }
              )
            }}
          </p>
          <img
              class="w-[50px] h-[50px] object-cover"
              :src="
              `http://openweathermap.org/img/wn/${day.weather[0].icon}@2x.png`
            "
              alt=""
          />
          <div class="flex gap-2 flex-1 justify-end">
            <p>H: {{ Math.round(day.temp.max) }}</p>
            <p>L: {{ Math.round(day.temp.min) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from "axios";
import { useRoute } from "vue-router";
const route = useRoute();
const getWeatherData = async () => {
  try {
    const weatherData = await axios.get(
        `https://api.openweathermap.org/data/2.5/onecall?lat=${route.query.lat}&lon=${route.query.lng}&exclude={part}&appid=7efa332cf48aeb9d2d391a51027f1a71&units=metric`
    );
    // Подсчёт текущей даты и времени
    const localOffset = new Date().getTimezoneOffset() * 60000;
    const utc = weatherData.data.current.dt * 1000 + localOffset;
    weatherData.data.currentTime =
        utc + 1000 * weatherData.data.timezone_offset;
    // Подсчет почасовой погоды
    weatherData.data.hourly.forEach((hour) => {
      const utc = hour.dt * 1000 + localOffset;
      hour.currentTime =
          utc + 1000 * weatherData.data.timezone_offset;
    });
    return weatherData.data;
  } catch (err) {
    console.log(err);
  }
};
const weatherData = await getWeatherData();
</script>