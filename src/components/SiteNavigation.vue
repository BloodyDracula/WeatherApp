
<template>
  <header class="sticky top-0 bg-weather-primary shadow-lg">
    <nav class="container flex flex-col sm:flex-row items-center gap-4 text-white py-6"
    >
      <RouterLink :to="{name: 'home'}">
      <div class="flex items-center gap-3">
        <i class="fa-solid fa-sun text-2xl"></i>
        <p class="text-2xl">Местная погода</p>
      </div>
      </RouterLink>

      <div class="flex gap-3 flex-1 justify-end">
        <i class="fa-solid fa-circle-info text-xl hover:text-weather-secondary duration-150 cursor-pointer" @click="toggleModal"
        ></i>
        <i class="fa-thin fa-plus text-xl hover:text-weather-secondary duration-150 cursor-pointer"
           @click="addCity"
           v-if="route.query.preview"
        ></i>
      </div>

      <BaseModal
                 :modalActive="modalActive"
                 @close-modal="toggleModal"
      >
        <div class="text-black">
          <h1 class="text-2xl mb-1">О сайте:</h1>
          <p class="mb-4">
            Этот сайт может показывать вам текущую и будущую погоду
            а также вы можете здесь выбрать города и отслеживать их
          </p>
          <h2 class="text-2xl">Как это работает:</h2>
          <ol class="list-decimal list-inside mb-4">
            <li>
              Введите в поиске ваш город
            </li>
            <li>
              В результатах поиска выберите нужный вам город,
              это покажет вам текущую и будущую погоду
            </li>
            <li>
              Чтобы добавить город в отслеживаемые нажмите +
              в правом верхнем углу
            </li>
          </ol>

          <h2 class="text-2xl">Убрать город</h2>
          <p>

          </p>
        </div>
      </BaseModal>
    </nav>
  </header>
</template>

<script setup>
import {uid} from 'uid'
import BaseModal from "./BaseModal.vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import {ref} from "vue";

const route = useRoute();
const router = useRouter();
const savedCities = ref([]);
const addCity = () => {
  if (localStorage.getItem('savedCities')) {
    savedCities.value = JSON.parse(localStorage.getItem('savedCities'));
  }
  const locationObj = {
    id: uid(),
    state: route.params.state,
    city: route.params.city,
    coords: {
      lat: route.query.lat,
      lng: route.query.lng,
    },
  };

  savedCities.value.push(locationObj);
  localStorage.setItem('savedCities', JSON.stringify(savedCities.value));

  let query = Object.assign({}, route.query);
  delete query.preview;
  query.id = locationObj.id;
  router.replace({query})
};


const modalActive = ref(null);
const toggleModal = () => {
  modalActive.value = !modalActive.value;
};
</script>
