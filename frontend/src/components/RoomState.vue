<template>

<v-data-table
    :headers="headers"
    :items="roomState"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'RoomState',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "roomNumber", value: "roomNumber" },
            { text: "roomStatus", value: "roomStatus" },
            { text: "roomUsedCount", value: "roomUsedCount" },
            { text: "conferenceId", value: "conferenceId" },
            { text: "conferenceStatus", value: "conferenceStatus" },
        ],
        roomState : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/roomstates')

      this.roomState = temp.data._embedded.roomstates;

    },
    methods: {
    }
  }
</script>

